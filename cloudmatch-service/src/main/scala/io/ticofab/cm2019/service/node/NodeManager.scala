package io.ticofab.cm2019.service.node

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.stream.SourceRef
import io.ticofab.cm2019.service.api.{Server, SystemController}
import io.ticofab.cm2019.service.model.Messages._
import io.ticofab.cm2019.service.node.NodeManager.{FlowSource, GetFlowSource}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NodeManager extends Actor with LogSupport {
  info(s"starting, $self")

  // create this node's publisher
  val publisher = context.actorOf(Publisher(), "publisher")

  // create this node's http server
  implicit val as: ActorSystem = context.system
  new Server(SystemController.route(self.path.name))

  override def receive: Receive = {

    case cmw: CheckMatchingWith =>
      // asks all my kids if they match
      context.children.foreach(_ forward cmw)

    case DeviceConnected(id, location) =>
      // spawn a new actor for each phone
      logger.info(s"a device connected!")
      val name   = s"${self.path.name}_${context.children.size + 1}"
      val device = context.actorOf(Device(id, location, publisher), name)
      (device ? GetFlowSource) (3.seconds)
        .mapTo[FlowSource]
        .map(flowSource => DeviceActorReady(self, device, location, flowSource.ref))
        .pipeTo(sender)

    case Welcome =>
      logger.info(s"I have been welcomed by $sender")
      sender ! RegisterNode

  }

}

object NodeManager {

  def apply(): Props = Props(new NodeManager)

  case object GetFlowSource

  case class FlowSource(ref: SourceRef[String])

}
