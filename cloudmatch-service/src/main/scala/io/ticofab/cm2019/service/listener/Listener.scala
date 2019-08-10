package io.ticofab.cm2019.service.listener

import akka.actor.{Actor, ActorRef, ActorSystem, Props, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberUp}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Sink}
import akka.stream.{ActorMaterializer, FlowShape}
import io.ticofab.cm2019.service.config.Config
import io.ticofab.cm2019.service.model.Messages._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class Listener extends Actor with LogSupport {
  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  info(s"starting, $self")

  // my state: the connected nodes
  var nodes: Map[ActorRef, Int] = Map()

  override def preStart(): Unit = {
    // when this is starting, we know that the cluster has been formed
    // make sure that we register only we we are effectively up
    val cluster = Cluster(context.system)
    cluster registerOnMemberUp {
      logger.info(s"$self, I'm up at address ${cluster.selfAddress}. Follows cluster state:")
      logger.info(s"  members: ${cluster.state.members}")
      logger.info(s"  leader: ${cluster.state.leader}")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberUp])
    }
  }

  def receive: Receive = {
    case MemberUp(m) =>
      val nodeManagerAddress = RootActorPath(m.address) / "user" / "node-manager"
      if (m.address != Cluster(context.system).selfAddress) {
        logger.info(s"a node joined: ${m.address}, full address: $nodeManagerAddress")
        context.actorSelection(nodeManagerAddress) ! Welcome
      }

    case RegisterNode =>
      debug(s"node registered: $sender")
      nodes = nodes + (sender -> 0)

    case dc: DeviceConnected =>
      // forward it to node with minimum number of connected phones
      val chosenNode = {
        val nodesForLog = nodes.map { case (node: ActorRef, devicesOnIt: Int) => (node.path.name, devicesOnIt) }
        val nodeForDevice = nodes.minBy(_._2)._1
        debug(s"who to send this device to? loads are $nodesForLog. Sending it to ${nodeForDevice.path.name}")
        nodeForDevice
      }

      (chosenNode ? dc) (3.seconds)
        .mapTo[DeviceActorReady]
        .map { case DeviceActorReady(manager, deviceActor, deviceLocation, messagesSource) =>

          // some side effecting stuff
          debug(s"device actor is ready for location $deviceLocation: $deviceActor")
          val updatedLoad = nodes.getOrElse(manager, 0) + 1
          nodes = nodes + (manager -> updatedLoad)

          // if all nodes have maximum capacity, scale up
          val maxCapacityReached = nodes.forall { case (_, load) => load == Config.cloudmatch.`max-devices-per-node` }
          if (maxCapacityReached) {
            info("max capacity reached, scaling up")
            // TODO: scale up (add kubernetes controller)
            // TODO: see if I can make it work with Akka Cluster Bootstrap
          }

          nodes.foreach { case (node, _) => node ! CheckMatchingWith(deviceActor, deviceLocation) }

          // create and send flow back
          Flow.fromGraph(GraphDSL.create() { implicit b =>

            val textMsgFlow = b.add(Flow[Message]
              .mapAsync(1) {
                case tm: TextMessage => tm.toStrict(3.seconds).map(_.text)
                case _ => Future.failed(new Exception("yuck"))
              })

            val pubSrc = b.add(messagesSource.map(TextMessage(_)))

            textMsgFlow ~> Sink.foreach[String](s => deviceActor ! MessageForMatchedDevice(Message(s)))
            FlowShape(textMsgFlow.in, pubSrc.out)
          })
        }
        .pipeTo(sender)
  }

  // start actors
  context.actorOf(Props(new DeviceController))
}

object Listener {
  def apply(): Props = Props(new Listener())
}
