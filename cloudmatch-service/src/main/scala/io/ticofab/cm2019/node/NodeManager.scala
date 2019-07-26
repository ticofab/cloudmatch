package io.ticofab.cm2019.node

import akka.actor.{Actor, Props, RootActorPath}
import akka.pattern.{ask, pipe}
import akka.stream.SourceRef
import io.ticofab.cm2019.common.Messages.{CheckMatchingWith, DeviceActorReady, DeviceConnected, RegisterNode}
import io.ticofab.cm2019.node.NodeManager.{FlowSource, GetFlowSource}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class NodeManager extends Actor with LogSupport {
  info(s"starting, name is ${self.path.name}")

  override def preStart(): Unit = {
    // for the moment, this is the only node manager
    context.actorSelection("akka://cm2019/user/listener") ! RegisterNode
  }

  override def receive: Receive = {
    case cmw: CheckMatchingWith =>
      // asks all my kids if they match
      context.children.foreach(_ forward cmw)

    case DeviceConnected(location) =>
      // spawn a new actor for each phone
      val name = s"${self.path.name}_${context.children.size + 1}"
      val device = context.actorOf(Props(new Device(location)), name)
      (device ? GetFlowSource) (3.seconds)
        .mapTo[FlowSource]
        .map(flowSource => DeviceActorReady(self, device, location, flowSource.ref))
        .pipeTo(sender)
  }

}

object NodeManager {

  def apply(): Props = Props(new NodeManager)

  case object GetFlowSource

  case class FlowSource(ref: SourceRef[String])

}
