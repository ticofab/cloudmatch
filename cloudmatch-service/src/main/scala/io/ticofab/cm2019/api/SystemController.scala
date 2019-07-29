package io.ticofab.cm2019.api

import akka.http.scaladsl.server.Route
import wvlet.log.LogSupport

object SystemController extends Controller with LogSupport {
  override val route: Route =
    extractClientIP { ip =>
      val clientIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
      pathPrefix("system") {
        path("readiness") {
          get {
            logger.info(s"received readiness request from $clientIp")
            complete("CloudMatch Service is ready!")
          }
        } ~ path("liveness") {
          get {
            logger.info(s"received alive request from $clientIp")
            complete("CloudMatch Service is alive!")
          }
        }
      }
    }
}
