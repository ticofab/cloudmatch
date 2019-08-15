package io.ticofab.cm2019.stream

import akka.actor.ActorSystem
import io.ticofab.cm2019.stream.api.{Server, SystemController}
import io.ticofab.cm2019.stream.config.Config
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import pureconfig.generic.auto._
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

object CloudMatchStreamApp extends App with LogSupport {
  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)

  info("cloudmatch-stream app starting!")

  // start http server
  implicit val as: ActorSystem = ActorSystem("cloudmatch")
  new Server(SystemController.route)

  // load config
  var config        = pureconfig.loadConfigOrThrow[Config]

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val connectionConfig = {
    val connectionUri = "amqp://" + config.amqp.username + ":" + config.amqp.password + "@dove.rmq.cloudamqp.com/mjvoypan"
    new RMQConnectionConfig.Builder().setUri(connectionUri).build
  }

  val stream = env
    .addSource(new RMQSource[String](
      connectionConfig, // config for the RabbitMQ connection
      config.amqp.queueName, // name of the RabbitMQ queue to consume
      true, // use correlation ids; can be false if only at-least-once is required
      new SimpleStringSchema)) // deserialization schema to turn messages into Java objects
    .setParallelism(1) // non-parallel source is only required for exactly-once

  stream.print()
  env.execute()
}
