package io.ticofab.cm2019.node

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.pipe
import akka.stream.scaladsl.{Keep, Sink, Source, StreamRefs}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import io.ticofab.cm2019.model.Messages.{CheckMatchingWith, Message, MessageForMatchedDevice, YouMatchedWith}
import io.ticofab.cm2019.model.Location
import io.ticofab.cm2019.node.NodeManager.{FlowSource, GetFlowSource}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Device(myId: String, myLocation: Location) extends Actor with LogSupport {

  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  info(s"phone actor ${self.path.name} created for id $myId and location $myLocation")
  // TODO: send request to db about connectionOpen

  val (down: ActorRef, futureFlowSource: Future[FlowSource]) = {
    val (down, publisher) = Source
      .actorRef[String](1000, OverflowStrategy.fail)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both)
      .run()
    val futureFlowSource = Source.fromPublisher(publisher).runWith(StreamRefs.sourceRef()).map(FlowSource)
    (down, futureFlowSource)
  }

  var matchedDevice: Option[ActorRef] = None

  override def receive: Receive = {
    case GetFlowSource => futureFlowSource
      .pipeTo(sender)
      .andThen { case _ => down ! s"Connected and handled by ${self.path.name}" }

    case CheckMatchingWith(device, itsLocation) =>
      debug(s"${self.path.name}, I got asked if I match ${device.path.name}")
      if (device != self && myLocation.isCloseEnoughTo(itsLocation)) {
        matchedDevice = Some(device)
        logMatched(self, device)
        down ! tellMatched(device)
        device ! YouMatchedWith(self)
      }

    case YouMatchedWith(device) =>
      matchedDevice = Some(device)
      down ! tellMatched(device)
      logMatched(self, device)

    case MessageForMatchedDevice(msg) =>
      debug(s"${self.path.name}, received a message for my matched device: $msg")
      // TODO: send event to DB about bytes sent
      matchedDevice.foreach(_ ! msg)

    case msg: Message =>
      debug(s"${self.path.name}, received a message for my own device from ${sender.path.name}: $msg")
      down ! s"device ${sender.path.name} says: ${msg.content}"
  }

  // shortcuts
  def logMatched(me: ActorRef, it: ActorRef): Unit = info(s"phone ${me.path.name}, matched with phone ${it.path.name}")

  def tellMatched(matchedWith: ActorRef): String = s"I matched with device '${matchedWith.path.name}'!"

}

object Device{
  def apply(id: String, location: Location): Props = Props(new Device(id, location))
}

