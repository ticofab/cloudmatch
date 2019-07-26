package io.ticofab.cm2019.listener

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import io.ticofab.cm2019.common.Location
import io.ticofab.cm2019.common.Messages.DeviceConnected
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Server extends Actor with LogSupport {
  type HandlingFlow = Flow[Message, Message, _]

  override def receive: Receive = Actor.emptyBehavior

  implicit val as: ActorSystem = context.system

  // http server to control the rate per second of inputs
  implicit val am: ActorMaterializer = ActorMaterializer()
  val routes: Route = path("connect") {
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
  } ~ path("healthz") {
    get {
      // curl http://0.0.0.0:8080
      complete("Phone App Listener is alive!\n")
    }
  }

  info("CloudMatch server listening on port 8080")
  Http().bindAndHandle(routes, "0.0.0.0", 8080)
}
