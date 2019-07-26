package io.ticofab.cm2019.listener

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Sink}
import akka.stream.{ActorMaterializer, FlowShape}
import com.typesafe.config.ConfigFactory
import io.ticofab.cm2019.common.Messages._
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

  def receive: Receive = {
    case RegisterNode =>
      debug(s"node joined: $sender")
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
        .map { case DeviceActorReady(manager, deviceActor, itsLocation, sourceRef) =>

          // some side effecting stuff
          debug(s"device actor is ready for location $itsLocation: $deviceActor")
          val updatedLoad = nodes.getOrElse(manager, 0) + 1
          nodes = nodes + (manager -> updatedLoad)

          // if all nodes have maximum capacity, scale up
          val maxCapacityReached = nodes.forall { case (_, load) => load == Listener.maxActorsPerNode }
          if (maxCapacityReached) {
            info("max capacity reached, scaling up")
            // TODO: scale up (add kubernetes controller)
            // TODO: see if I can make it work with Akka Cluster Bootstrap
          }

          nodes.foreach { case (node, _) => node ! CheckMatchingWith(deviceActor, itsLocation) }

          // create and send flow back
          Flow.fromGraph(GraphDSL.create() { implicit b =>

            val textMsgFlow = b.add(Flow[Message]
              .mapAsync(1) {
                case tm: TextMessage => tm.toStrict(3.seconds).map(_.text)
                case _ => Future.failed(new Exception("yuck"))
              })

            val pubSrc = b.add(sourceRef.map(TextMessage(_)))

            textMsgFlow ~> Sink.foreach[String](s => deviceActor ! MessageForMatchedDevice(Message(s)))
            FlowShape(textMsgFlow.in, pubSrc.out)
          })
        }
        .pipeTo(sender)
  }

  // start actors
  context.actorOf(Props(new Server))
}

object Listener {
  val maxActorsPerNode: Int = ConfigFactory.load.getInt("max-devices-per-node")

  def apply(): Props = Props(new Listener)
}
