package br.com.jityk.shipsimulator.actor

import akka.actor.Actor
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.HttpClients

/**
  * Created by jferreira on 2/8/16.
  */
class ForwarderActor extends Actor {
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
    if (configured) {
      val httpClient = HttpClients.createDefault()
      val sendStr = Seq(msg.imoNumber,msg.position.latitude,msg.position.longitude,msg.timestamp).mkString(",")
      httpClient.execute(new HttpGet(simFrontEndBaseUrl + "/report/" + sendStr))
    } else {
//      println("imo=" + msg.imoNumber + " lat=" + msg.position.latitude + " lon=" + msg.position.longitude + " time=" + msg.timestamp)
    }
  }

}
