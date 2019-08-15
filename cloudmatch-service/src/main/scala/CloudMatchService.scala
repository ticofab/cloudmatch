import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import io.ticofab.cm2019.service.config.Config
import io.ticofab.cm2019.service.listener.Listener
import io.ticofab.cm2019.service.node.{NodeManager, Publisher}
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

object CloudMatchService extends App with LogSupport {
  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)

  info(s"cloudmatch service starting, role: ${Config.cloudmatch.role}")

  implicit val system: ActorSystem = ActorSystem("cloudmatch")

  val cluster = Cluster(system)

  // Start application after self member joined the cluster (Up)
  cluster.registerOnMemberUp {
    logger.info(s"onMember up, config is ${Config.cloudmatch}")
  }

  if (Config.cloudmatch.role == "node") {
    logger.info("starting a node")
    system.actorOf(NodeManager(), "node-manager")
  } else {
    logger.info("starting a listener")
    system.actorOf(Listener(), "listener")
  }

  bootstrapCluster()

  def bootstrapCluster()(implicit system: ActorSystem): Unit = {

    // Starting Akka Cluster Management endpoint
    AkkaManagement(system).start()

    // Initiating Akka Cluster Bootstrap procedure
    ClusterBootstrap(system).start()

  }
}


