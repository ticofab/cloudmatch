package io.ticofab.cm2019.model

case class PEvent(eventId: String,
                  deviceId: String,
                  eventType: String,
                  amount: Int)

object PEvent {
  // TODO: this is ugly I know, I would never use this in production
  def apply(tuple: (String, String, String, Int)): PEvent = PEvent(tuple._1, tuple._2, tuple._3, tuple._4)
}
