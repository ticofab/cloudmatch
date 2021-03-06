package io.ticofab.cm2019.service.config

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

object Config {
  private val config: com.typesafe.config.Config = ConfigFactory.load()

  case class CloudMatchConfig(`max-devices-per-node`: Int,
                              `db-base-url`: String,
                              role: String)

  case class AmqpConfig(`queue-name`: String,
                        username: String,
                        password: String)

  val cloudmatch: CloudMatchConfig = config.as[CloudMatchConfig]("cloudmatch")
  val amqp      : AmqpConfig       = config.as[AmqpConfig]("amqp")
}


