package io.ticofab.cm2019.common.api

import akka.http.scaladsl.server.{Directives, Route}

trait Controller extends Directives {
  val route: Route
}
