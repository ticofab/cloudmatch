package io.ticofab.cm2019.service.node

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, Uri}
import akka.pattern.pipe
import akka.stream.scaladsl.{Keep, Sink, Source, StreamRefs}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import io.ticofab.cm2019.service.model.Messages.{CheckMatchingWith, Message, MessageForMatchedDevice, YouMatchedWith}
import io.ticofab.cm2019.service.model.{Event, JsonSupport, Location}
import io.ticofab.cm2019.service.node.NodeManager.{FlowSource, GetFlowSource}
import io.ticofab.cm2019.service.node.Publisher.Publish
import spray.json._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Device(myId: String, myLocation: Location, publisher: ActorRef) extends Actor with JsonSupport with LogSupport {

  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  info(s"phone actor ${self.path.name} created for id $myId and location $myLocation")

  // persist the event that this device connected
  persistEvent(Event(myId, "connectionsOpen", 1))

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
      matchedDevice.foreach(_ ! msg)
      persistEvent(Event(myId, "bytesSent", msg.content.getBytes().length))

    case msg: Message =>
      debug(s"${self.path.name}, received a message for my own device from ${sender.path.name}: $msg")
      publisher ! Publish(msg.content.length.toString)
      down ! s"device ${sender.path.name} says: ${msg.content}"
  }

  // shortcuts
  private def logMatched(me: ActorRef, it: ActorRef): Unit = info(s"phone ${me.path.name}, matched with phone ${it.path.name}")

  private def tellMatched(matchedWith: ActorRef): String = s"I matched with device '${matchedWith.path.name}'!"

  private def persistEvent(event: Event): Unit = {
    val req = HttpRequest(HttpMethods.POST, Uri("http://cloudmatch-db-svc:81/device/event"))
      .withEntity(ContentTypes.`application/json`, event.toJson.compactPrint)
    val futureProcessedResp = for {
      resp <- Http().singleRequest(req)
      body <- resp.entity.toStrict(1.second)
    } yield (resp.status, body.data.utf8String)
    futureProcessedResp.onComplete {
      case Success((status, msg)) => logger.info(s"posted event response: $status, $msg")
      case Failure(err) => logger.error(s"failed persisting event: ${err.getMessage}", err)
    }
  }
}

object Device {
  def apply(id: String, location: Location, publisher: ActorRef): Props = Props(new Device(id, location, publisher))
}

