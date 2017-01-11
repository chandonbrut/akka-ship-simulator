package br.com.jityk.shipsimulator.actor

import akka.actor.Actor
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.Inject
import play.api.Logger

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jferreira on 2/8/16.
  */
class ForwarderActor extends Actor {
  implicit val materializer = ActorMaterializer()
  val wsClient = AhcWSClient()

  var simFrontEndBaseUrl = ""
  var configured = false

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
      "latitude" -> msg.position.latitude,
      "longitude" -> msg.position.longitude
    )


    if (configured) {
      val sendStr = simFrontEndBaseUrl

      wsClient.url(sendStr).withRequestTimeout(50 seconds).post(jsonObj).onComplete{

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
