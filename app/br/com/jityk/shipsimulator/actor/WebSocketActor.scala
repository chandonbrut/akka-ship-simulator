package br.com.jityk.shipsimulator.actor

import akka.actor._
import play.api.libs.json.Json

/**
  * Created by jonasferreira on 2/23/16.
  */
object WebSocketActor {
  def props(out: ActorRef, sim:ActorRef) = Props(new WebSocketActor(out, sim))
}

class WebSocketActor(out:ActorRef, sim:ActorRef) extends Actor {


  implicit val pointReads = Json.reads[Point]
  implicit val pointWrites = Json.writes[Point]

  implicit val reportReads = Json.reads[Report]
  implicit val reportWrites = Json.writes[Report]



  def receive = {
    case "register" => {
      println(sim)
      println("Vou me registrar no manager...")
      sim ! Register()
    }
    case msg:Report => out ! Json.toJson(msg).toString()
  }

}