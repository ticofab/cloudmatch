package io.ticofab.cm2019.stream

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig

object CloudMatchStreamApp extends App {

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val connectionConfig = new RMQConnectionConfig.Builder()
    .setUri("amqp://mjvoypan:HFOUsyFLGvTOitP3F_2N1KzRJfgwv4yN@dove.rmq.cloudamqp.com/mjvoypan")
    .build

  val stream = env
    .addSource(new RMQSource[String](
      connectionConfig, // config for the RabbitMQ connection
      "queueName", // name of the RabbitMQ queue to consume
      true, // use correlation ids; can be false if only at-least-once is required
      new SimpleStringSchema)) // deserialization schema to turn messages into Java objects
    .setParallelism(1) // non-parallel source is only required for exactly-once


}
