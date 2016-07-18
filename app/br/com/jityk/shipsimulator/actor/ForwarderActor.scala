package br.com.jityk.shipsimulator.actor

import akka.actor.Actor
import play.api.Logger
import play.api.libs.ws.WS
import play.api.libs.ws.ning.NingWSClient

import scala.util.{Failure, Success}
import play.api.Play.current
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jferreira on 2/8/16.
  */
class ForwarderActor extends Actor {
  var simFrontEndBaseUrl = ""
  var configured = false
  implicit val sslClient = NingWSClient()

  def receive = {
    case msg:Report => sendToDC(msg)
    case msg:Configuration => {
      simFrontEndBaseUrl = msg.simFrontEndBaseUrl
      configured = true
    }
  }

  private def sendToDC(msg:Report) = {

    val jsonObj = Json.obj(
      "imoNumber" -> msg.imoNumber,
      "timestamp" -> msg.timestamp,
      "lat" -> msg.position.latitude,
      "lon" -> msg.position.longitude
    )


    if (configured) {
      val sendStr = simFrontEndBaseUrl
      WS.url(sendStr).withRequestTimeout(50000).post(jsonObj).onComplete{
        case Success(value) => {
          if (value.status == 200) Logger.info(msg.toString)
          else Logger.error(value.statusText + " error")
        }
        case Failure(fail) => {
          Logger.error("Erro enviando:" + msg.toString + " " + fail)
        }
      }
    } else {
      Logger.info(msg.toString)
    }
  }

}
