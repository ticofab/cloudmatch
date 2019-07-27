package io.ticofab.cm2019.common.api

import akka.http.scaladsl.server.Route
import wvlet.log.LogSupport

object SystemController extends Controller with LogSupport {
  override val route: Route =
    pathPrefix("system") {
      path("readiness") {
        get {
          complete("ready!")
        }
      } ~ path("liveness") {
        get {
          complete("alive!")
        }
      }
    }
}
