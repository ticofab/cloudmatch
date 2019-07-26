import akka.actor.{ActorSystem, Props}
import io.ticofab.cm2019.listener.Listener
import io.ticofab.cm2019.node.NodeManager
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

object CloudMatchService extends App with LogSupport {

  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)
  info("cm2019 service starting")

  val as = ActorSystem("cm2019")

  as.actorOf(Listener(), "listener")
  as.actorOf(NodeManager(), "single-node-manager")
}
