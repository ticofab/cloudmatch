package io.ticofab.cm2019.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import io.ticofab.cm2019.config.Config
import io.ticofab.cm2019.model.Event
import spray.json._

import scala.concurrent.Future

object PersistenceService extends DefaultJsonProtocol {

  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat3(Event)

  def persistEvent(event: Event)(implicit as: ActorSystem): Future[HttpResponse] = {
    val uri = Uri(Config.cloudmatch.`db-base-url` + "/device/event")
    val entity = HttpEntity(ContentTypes.`application/json`, event.toJson.compactPrint)
    val request = HttpRequest(HttpMethods.POST, uri, entity = entity)
    Http().singleRequest(request)
  }

  def getState(deviceId: String)(implicit as: ActorSystem): Future[HttpResponse] = {
    val uri = Uri(Config.cloudmatch.`db-base-url` + "/device/state/" + deviceId)
    val request = HttpRequest(uri = uri)
    Http().singleRequest(request)
  }
}
