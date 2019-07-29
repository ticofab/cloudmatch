package io.ticofab.cm2019.model

case class State(deviceId: String, connectionsOpen: Int, bytesSent: Int) {
  def isEmpty: Boolean = connectionsOpen == 0 && bytesSent == 0
}

