package br.com.jityk.shipsimulator.actor

import akka.actor.{Props, PoisonPill, ActorRef, Actor}
import akka.actor.Actor.Receive

/**
  * Created by jferreira on 2/9/16.
  */
class SimulationActor extends Actor {

  private var manager:ActorRef = null

  override def receive = {
    case msg:Configuration => {
      if (manager != null) {
        context.system.stop(manager)
        context.unwatch(manager)
      }
      manager = context.actorOf(Props(new ManagerActor(msg)),"manager")
    }
    case msg:Register => {
      manager forward msg
    }

    case msg:OneTimePoll => manager ! msg
    case msg:ChangeRate => manager ! msg
    case msg:StopSimulation => manager ! msg
  }

}
