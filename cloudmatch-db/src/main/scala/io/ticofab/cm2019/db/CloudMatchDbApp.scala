package io.ticofab.cm2019.db

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.ticofab.cm2019.db.api.{Event, Server, State, SystemController}
import io.ticofab.cm2019.db.model.PEvent
import slick.jdbc.H2Profile.api._
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CloudMatchDbApp extends App with Directives with LogSupport {

  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)

  // DB SETUP
  val db = Database.forConfig("h2mem1")

  class Events(tag: Tag) extends Table[(String, String, String, Int)](tag, "EVENTS") {
    def eventId = column[String]("EVENT_ID", O.PrimaryKey)

    def deviceId = column[String]("DEVICE_ID")

    def eventType = column[String]("EVENT_TYPE")

    def amount = column[Int]("AMOUNT")

    override def * = (eventId, deviceId, eventType, amount)
  }

  val events = TableQuery[Events]

  db.run(DBIO.seq(
    // Create the table, including keys
    events.schema.create
  )).onComplete {
    case Success(_) => logger.info(s"table creation operation successful")
    case Failure(err) => logger.error("table creation operation failed", err)
  }

  // test
  //  db.run(events += (UUID.randomUUID().toString, "1", "bytesSent", 300))
  //    .flatMap(_ => db.run(events += (UUID.randomUUID().toString, "1", "bytesSent", 400)))
  //    .flatMap(_ => db.run(events += (UUID.randomUUID().toString, "2", "bytesSent", 400)))
  //  db.run(events.result).foreach(seq => println("all seq " + seq))
  //  db.run(events.filter(_.deviceId === "2").result).foreach(seq => println("one seq: " + seq))

  // routes to listen to events
  val route =
    extractClientIP { ip =>
      val clientIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
      pathPrefix("device") {
        path("event") {
          post {
            entity(as[Event]) { event =>
              logger.info(s"post event request from ip $clientIp. event: $event")
              onComplete(db.run(events += (UUID.randomUUID().toString, event.deviceId, event.eventType, event.amount))) {
                case Success(_) => complete("Thanks!")
                case Failure(error) => complete((InternalServerError, "Failure: " + error.getMessage))
              }
            }
          }
        } ~ path("state" / Segment) { deviceId =>
          get {
            logger.info(s"get device state request from ip $clientIp. device: $deviceId")
            val futureState = db.run(events.filter(_.deviceId === deviceId).result).map(seq =>
              seq.map(PEvent.apply).foldLeft(State(deviceId, 0, 0))((stateAcc, currentEvent) => currentEvent.eventType match {
                case "connectionsOpen" => State(deviceId, stateAcc.connectionsOpen + currentEvent.amount, stateAcc.bytesSent)
                case "bytesSent" => State(deviceId, stateAcc.connectionsOpen, stateAcc.bytesSent + currentEvent.amount)
                case unknownType => throw new Exception(s"unknown event type $unknownType")
              }))
            onComplete(futureState) {
              case Success(state) =>
                if (state.isEmpty) complete((NotFound, s"deviceId $deviceId could not be found"))
                else complete(state)
              case Failure(error) => complete((InternalServerError, "Failure: " + error.getMessage))
            }
          }
        }
      }
    }

  implicit val as: ActorSystem = ActorSystem("cloudmatch")
  new Server(route ~ SystemController.route)
}


