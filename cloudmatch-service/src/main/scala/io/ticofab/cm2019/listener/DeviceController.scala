package io.ticofab.cm2019.listener

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import io.ticofab.cm2019.api.{Controller, Server, SystemController}
import io.ticofab.cm2019.model.Location
import io.ticofab.cm2019.model.Messages.DeviceConnected
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
  val route: Route = pathPrefix("device") {
    path("connect") {
      // curl http://0.0.0.0:8080/connect?id=a&lat=1&lon=2
      parameters("id".as[String], "lat".as[Int], "lon".as[Int]) { (id, lat, lon) =>
        info(s"phone connected at location ($lat, $lon)")
        val handlingFlow = (context.parent ? DeviceConnected(id, Location(lat, lon))) (3.seconds).mapTo[HandlingFlow]
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
  }

  // start actual server
  new Server(route ~ SystemController.route)
}
