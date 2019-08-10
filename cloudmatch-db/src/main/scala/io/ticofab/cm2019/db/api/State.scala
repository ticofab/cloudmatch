package io.ticofab.cm2019.db.api

case class State(deviceId: String, connectionsOpen: Int, bytesSent: Int) {
  def isEmpty: Boolean = connectionsOpen == 0 && bytesSent == 0
}
