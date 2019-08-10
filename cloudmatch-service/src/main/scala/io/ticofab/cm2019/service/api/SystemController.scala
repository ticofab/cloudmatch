package io.ticofab.cm2019.service.api

import akka.http.scaladsl.server.{Directives, Route}
import wvlet.log.LogSupport

object SystemController extends Directives with LogSupport {
  def route(instanceName: String): Route =
    extractClientIP { ip =>
      val clientIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
      pathPrefix("system") {
        path("readiness") {
          get {
            logger.info(s"$instanceName received readiness request from $clientIp")
            complete(s"CloudMatch Service ($instanceName) is ready!\n")
          }
        } ~ path("liveness") {
          get {
            logger.info(s"$instanceName received alive request from $clientIp")
            complete(s"CloudMatch Service ($instanceName) is alive!\n")
          }
        }
      }
    }
}
