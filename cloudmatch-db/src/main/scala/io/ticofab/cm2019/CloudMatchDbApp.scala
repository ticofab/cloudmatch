package io.ticofab.cm2019

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import io.ticofab.cm2019.api.{Event, Server, SystemController}
import slick.jdbc.H2Profile.api._
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object CloudMatchDbApp extends App with Directives with LogSupport {

  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)

  val db = Database.forConfig("h2mem1")

  class Events(tag: Tag) extends Table[(String, String, String, Int)](tag, "EVENTS") {
    def eventId = column[String]("EVENT_ID", O.PrimaryKey)

    def deviceId = column[String]("DEVICE_ID")

    def eventType = column[String]("EVENT_TYPE")

    def amount = column[Int]("AMOUNT")

    override def * = (eventId, deviceId, eventType, amount)
  }

  val events = TableQuery[Events]

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val route = path("event") {
    post {
      entity(as[Event]) { foo =>
        db.run(DBIO.seq(
          events += (UUID.randomUUID().toString, foo.deviceId, foo.eventType, foo.amount)
        )).onComplete(logDbOpOutcome("event insertion"))
        complete("Thanks!")
      }
    } ~ get {
      path(Segment) { deviceId =>
        for {
          event <- events if event.deviceId.toString() == deviceId
        } yield ()
          complete("yo")
      }
    }
  }

  implicit val as: ActorSystem = ActorSystem("cloudmatch")
  new Server(route ~ SystemController.route)

  db.run(DBIO.seq(
    // Create the table, including keys
    events.schema.create
  )).onComplete(logDbOpOutcome("table creation"))

  def logDbOpOutcome(op: String): PartialFunction[Try[Unit], Unit] = {
    case Success(_) => logger.info(s"db operation successful: $op")
    case Failure(err) => logger.error(s"operation failed: $op", err)
  }


}


