package io.ticofab.cm2019.api

import akka.http.scaladsl.server.{Directives, Route}

trait Controller extends Directives {
  val route: Route
}
