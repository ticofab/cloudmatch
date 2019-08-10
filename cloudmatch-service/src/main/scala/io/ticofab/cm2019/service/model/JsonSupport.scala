package io.ticofab.cm2019.service.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val eventJsonFormat: RootJsonFormat[Event] = jsonFormat3(Event)
  implicit val stateJsonFormat: RootJsonFormat[State] = jsonFormat3(State)
}
