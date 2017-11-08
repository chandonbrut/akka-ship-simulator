package br.com.jityk.shipsimulator.actor


import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{BroadcastRoutingLogic, Router}

import scala.concurrent.ExecutionContext.Implicits.global



/**
  * Created by jferreira on 2/8/16.
  */

class ManagerActor extends Actor {

  var websockets:Router = Router(BroadcastRoutingLogic())
  var simulations:List[(String,ActorRef,Configuration)] = List()

  override def receive = {
    case msg:StopSimulation => {
      println("Stopping simulation")
      val reference = simulations.filter(sim => sim._1 == msg.simulatorId)
      println(reference)
      reference.head._2 ! msg
      simulations = simulations.filterNot(_._1 == msg.simulatorId)
    }
    case msg:Register => {
      println("Registrando cliente WebSocket")
      websockets = websockets.addRoutee(sender)
    }
    case r:Report => {
      websockets.route(r,sender)
    }
    case msg:StartSimulation => {
      val simulation = context.actorOf(Props[SimulationActor])
      simulation ! msg.configuration
      simulations = (UUID.randomUUID().toString,simulation,msg.configuration) :: simulations
    }
    case msg:GetConfig => {
      val configs = simulations.map(simulation => SimulationStatus(simulation._1,simulation._3))

      if (configs != Nil) sender ! configs
      else sender ! List()
    }
    case msg:ChangeRate => {
      simulations.map(sim => sim._2 ! msg)
    }
  }



}
