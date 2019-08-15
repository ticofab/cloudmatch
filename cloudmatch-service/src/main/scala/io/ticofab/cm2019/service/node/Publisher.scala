package io.ticofab.cm2019.service.node

import akka.actor.{Actor, Props}
import com.newmotion.akka.rabbitmq.{Channel, Connection, ConnectionFactory}
import io.ticofab.cm2019.service.config.Config
import io.ticofab.cm2019.service.node.Publisher.Publish

class Publisher extends Actor {

  // setup
  val connectionUri = "amqp://" + Config.amqp.username + ":" + Config.amqp.password + "@dove.rmq.cloudamqp.com/mjvoypan"
  val factory       = new ConnectionFactory()
  factory.setUri(connectionUri)
  val connection: Connection = factory.newConnection()
  val channel   : Channel    = connection.createChannel()
  channel.queueDeclare(Config.amqp.`queue-name`, true, false, false, null)

  override def postStop() = {
    channel.close()
    connection.close()
  }

  override def receive: Receive = {
    case Publish(msg) => channel.basicPublish("", Config.amqp.`queue-name`, null, msg.getBytes)
  }
}

object Publisher {

  case class Publish(msg: String)

  def apply(): Props = Props(new Publisher)
}
