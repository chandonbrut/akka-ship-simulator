package br.com.jityk.shipsimulator.actor

import akka.actor._
import akka.routing.{BroadcastRoutingLogic, Router, ActorRefRoutee}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}


/**
  * Created by jferreira on 2/8/16.
  */

class ManagerActor(conf:Configuration) extends Actor {
  var websockets:Router = Router(BroadcastRoutingLogic())
  var router:Router = Router(BroadcastRoutingLogic())
  val forwarder = context.actorOf(Props[ForwarderActor])

  override def preStart() = {
    spawnShips(conf.numberOfShips, conf.wktArea, conf.imoFirstDigit)
    val simulationTickRate = Protocol.duration(conf.tickUnit)
    context.system.scheduler.schedule(0 seconds, simulationTickRate, self, Tick())
    forwarder ! conf
  }

  override def receive = {
    case msg:StopSimulation => {
      router.route(PoisonPill,self)
      router.removeRoutee(context.actorSelection("*"))
    }
    case msg:Register => {
      println("Registrando cliente WebSocket")
      websockets = websockets.addRoutee(sender)
    }
    case m:OneTimePoll => context.actorSelection(m.imoNumber) ! m
    case r:Report => {
      forwarder ! r
      websockets.route(r,sender)
    }
    case c:ChangeRate => context.actorSelection(c.imoNumber) ! c
    case t:Tick => router.route(Tick(),sender)
    case msg:Terminated => println("Ship " + sender.path.name + " stopped.")
  }

  private def spawnShips(numberOfShips:Int,wktArea:String, cgCode:Int) = {
    val base = cgCode * 1000000
    val children = (0 until numberOfShips).map(
      n => {
        val imoNumber = (base + n).toString
        println("Spawning ship " + imoNumber)
        val child = context.actorOf(Props(new ShipActor(imoNumber, wktArea, self)), imoNumber)
        context.watch(child)
        ActorRefRoutee(child)
      }
    )
    router = Router(BroadcastRoutingLogic(),children)
  }


}
