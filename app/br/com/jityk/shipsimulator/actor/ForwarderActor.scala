package br.com.jityk.shipsimulator.actor

import akka.actor.Actor
import play.api.Logger
import play.api.libs.ws.WS
import play.api.libs.ws.ning.NingWSClient

import scala.util.{Failure, Success}
import play.api.Play.current
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
    val outStr = "imo=" + msg.imoNumber + " lat=" + msg.position.latitude + " lon=" + msg.position.longitude + " time=" + msg.timestamp

    if (configured) {
      val sendStr = simFrontEndBaseUrl + "/report/" + Seq(msg.imoNumber,msg.position.latitude,msg.position.longitude,msg.timestamp).mkString(",")

      WS.url(sendStr).withRequestTimeout(4000).get().onComplete{
        case Success(value) => {
          if (value.status == 200) Logger.info(outStr)
          else Logger.error(outStr)
        }
        case Failure(fail) => {
          Logger.error(outStr)
        }
      }
    } else {
      Logger.info(outStr)
    }
  }

}
