package io.ticofab.cm2019.stream.api

import akka.http.scaladsl.server.{Directives, Route}
import wvlet.log.LogSupport

object SystemController extends Directives with LogSupport {
  val route: Route =
    extractClientIP { ip =>
      val clientIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
      pathPrefix("system") {
        path("readiness") {
          get {
            logger.info(s"received readiness request from $clientIp")
            complete("CloudMatch Stream is ready!\n")
          }
        } ~ path("liveness") {
          get {
            logger.info(s"received alive request from $clientIp")
            complete("CloudMatch Stream is alive!\n")
          }
        }
      }
    }
}
