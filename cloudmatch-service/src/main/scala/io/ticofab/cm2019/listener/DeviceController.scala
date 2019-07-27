package io.ticofab.cm2019.listener

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import io.ticofab.cm2019.common.Location
import io.ticofab.cm2019.common.Messages.DeviceConnected
import io.ticofab.cm2019.common.api.{Controller, Server, SystemController}
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class DeviceController extends Actor with Controller with LogSupport {

  type HandlingFlow = Flow[Message, Message, _]

  info(s"$self, starting")

  // this actor exclusively manages the listener WebSocket Server
  override def receive: Receive = Actor.emptyBehavior

  // plumbing
  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  // http server to control the rate per second of inputs
  val route: Route = path("connect") {
    // curl http://0.0.0.0:8080/connect?lat=1&lon=2
    parameters("lat".as[Int], "lon".as[Int]) { (lat, lon) =>
      info(s"phone connected at location ($lat, $lon)")
      val handlingFlow = (context.parent ? DeviceConnected(Location(lat, lon))) (3.seconds).mapTo[HandlingFlow]
      onComplete(handlingFlow) {
        case Success(flow) =>
          debug(s"received handling flow back from listener actor")
          handleWebSocketMessages(flow)
        case Failure(err) =>
          error(s"error in receiving handling flow back ${err.getMessage}", err)
          complete(HttpResponse(StatusCodes.InternalServerError))
      }
    }
  }

  // start actual server
  new Server(route ~ SystemController.route)
}
