package io.ticofab.cm2019.api

case class Event(eventId: String,
                 deviceId: String,
                 eventType: String,
                 amount: Int)

sealed trait EventType

object Event {

  case object NewConnection extends EventType

  case object BytesSent extends EventType

}
