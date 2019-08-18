package io.ticofab.cm2019.stream

import akka.actor.ActorSystem
import io.ticofab.cm2019.stream.api.{Server, SystemController}
import io.ticofab.cm2019.stream.config.Config
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import pureconfig.generic.auto._
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

import scala.util.{Failure, Success, Try}

object CloudMatchStreamApp extends App with LogSupport {
  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.WARN)

  info("cloudmatch-stream app starting!")

  // start http server
  implicit val as: ActorSystem = ActorSystem("cloudmatch")
  new Server(SystemController.route)

  // load config
  var config = pureconfig.loadConfigOrThrow[Config]

  // create config for AMQP connection
  val connectionConfig = {
    val connectionUri = "amqp://" + config.amqp.username + ":" + config.amqp.password + "@dove.rmq.cloudamqp.com/mjvoypan"
    new RMQConnectionConfig.Builder().setUri(connectionUri).build
  }

  // setup windowing stream
  class SumAggregate extends AggregateFunction[String, Success[Int], Int] with LogSupport {
    override def createAccumulator() = Success(0)

    override def add(value: String, accumulator: Success[Int]): Success[Int] = {
      Try(value.toInt) match {
        case Success(parsedInt) => Success(accumulator.value + parsedInt)
        case Failure(err) =>
          error("failure accumulating new value", err)
          accumulator
      }
    }

    override def getResult(accumulator: Success[Int]): Int = accumulator.value

    override def merge(a: Success[Int], b: Success[Int]) = Success(a.value + b.value)
  }

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val stream = env
    .addSource(new RMQSource[String](
      connectionConfig, // config for the RabbitMQ connection
      config.amqp.queueName, // name of the RabbitMQ queue to consume
      true, // use correlation ids; can be false if only at-least-once is required
      new SimpleStringSchema)) // deserialization schema to turn messages into Java objects
    .setParallelism(1) // non-parallel source is only required for exactly-once
    .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10)))
    .aggregate(new SumAggregate)
    .map(n => s"characters counted in the previous 10 seconds: $n")
    .print()

  env.execute()
}
