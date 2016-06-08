package br.com.jityk.shipsimulator.actor

import akka.actor.{PoisonPill, Props, ActorRef, Actor}

/**
  * Created by jferreira on 2/9/16.
  */
class SimulationActor extends Actor {

  private var manager:ActorRef = null
  private var config:Configuration = Configuration(null,-1,-1,-1,null)

  override def receive = {
    case msg:Configuration => {
      if (manager != null) {
        manager ! StopSimulation
        manager ! PoisonPill
        context.stop(manager)
        context.unwatch(manager)
      }
      manager = context.actorOf(Props(new ManagerActor(msg)))
      config = msg
    }
    case msg:Register => {
      manager forward msg
    }
    case msg:GetConfig => sender ! config
    case msg:OneTimePoll => manager ! msg
    case msg:ChangeRate => manager ! msg
    case msg:StopSimulation => manager ! msg
  }

}
