package io.ticofab.cm2019.common

import akka.actor.ActorRef
import akka.stream.SourceRef

object Messages {

  case object RegisterNode

  case class DeviceConnected(location: Location)

  case class DeviceActorReady(manager: ActorRef, deviceActor: ActorRef, location: Location, sourceRef: SourceRef[String])

  case class CheckMatchingWith(phone: ActorRef, location: Location)

  case class YouMatchedWith(device: ActorRef)

  case class MessageForMatchedDevice(msg: Message)

  case class Message(content: String)

  case object Welcome

}
