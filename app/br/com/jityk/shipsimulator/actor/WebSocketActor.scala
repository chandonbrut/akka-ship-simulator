package br.com.jityk.shipsimulator.actor

import akka.actor._

/**
  * Created by jonasferreira on 2/23/16.
  */
object WebSocketActor {
  def props(out: ActorRef, sim:ActorRef) = Props(new WebSocketActor(out, sim))
}

class WebSocketActor(out:ActorRef, sim:ActorRef) extends Actor {

  def receive = {
    case "register" => {
      println(sim)
      println("Vou me registrar no manager...")
      sim ! Register()
    }
    case msg:Report => out ! ("lat:" + msg.position.latitude + " lon:" + msg.position.longitude)
  }

}