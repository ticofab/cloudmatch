package io.ticofab.cm2019.stream.config

case class Amqp(queueName: String,
                username: String,
                password: String)

case class Config(amqp: Amqp)
