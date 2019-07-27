package io.ticofab.cm2019.common.api

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.actor.CoordinatedShutdown.{PhaseServiceRequestsDone, UnknownReason}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class Server(route: Route)(implicit system: ActorSystem) extends LogSupport with Directives {

  private implicit val mat: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val shutdown = CoordinatedShutdown(system)
  private val address = "0.0.0.0"
  private val port = 8080

  Http()
    .bindAndHandle(route, address, port)
    .onComplete {
      case Failure(_) =>
        logger.error(s"Shutting down because cannot bind to $address:$port")
        shutdown.run(UnknownReason)

      case Success(binding) =>
        logger.info(s"Listening for connections on ${binding.localAddress}")
    }

}

