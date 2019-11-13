package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import com.vividsolutions.jts.geom.{Geometry, GeometryFactory, LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout



/**
  * Created by jferreira on 2/9/16.
  */
class SimulationActor  extends Actor {

  private var manager:ActorRef = null
  private var config:Any = null
  private var children:List[ActorRef] = null

  var router:Router = Router(BroadcastRoutingLogic())
  val forwarder = context.actorOf(Props(new ForwarderActor()))


  private def stopRouters() = {
    router.route(PoisonPill,self)
    router.removeRoutee(context.actorSelection("*"))
  }

  private def startTimers() = {
    val simulationTickRate = Protocol.duration(config match {
      case c:Configuration => c.tickUnit
      case  c:OilConfiguration => c.tickUnit
    })
    context.system.scheduler.schedule(0 seconds, simulationTickRate, self, Tick())
  }

  private def spawnShips(numberOfShips:Int,wktArea:String, cgCode:Int) = {
    val base = cgCode * 1000000
    val children = (1 until numberOfShips+1).map(
      n => {
        val imoNumber = (base + n).toString
        println("Spawning ship " + imoNumber)
        val child = context.actorOf(Props(new ShipActor(imoNumber,wktArea, 1d, self)), s"shipActor-$imoNumber")
        context.watch(child)
        ActorRefRoutee(child)
      }
    )

    router = Router(BroadcastRoutingLogic(),children)
  }

  override def receive = {
    case msg:OilConfiguration => {
      stopRouters()
      config = msg
      val child = context.actorOf(Props(new OilActor(self,msg.oilId,msg.wktArea,msg.wktOilShape)),"oilActor")
      context.watch(child)
      val children = scala.collection.immutable.IndexedSeq(ActorRefRoutee(child))
      router = Router(BroadcastRoutingLogic(),children)
      manager = sender()
      startTimers()
    }
    case msg:Configuration => {
      stopRouters()
      config = msg
      forwarder ! config
      manager = sender
      spawnShips(msg.numberOfShips,msg.wktArea,msg.imoFirstDigit)
      startTimers()
    }
    case msg:Register => {
      manager forward msg
    }
    case msg:GetConfig => sender ! config
    case msg:StopSimulation => {
      println("Stopping simulation")
      stopRouters()
      forwarder ! PoisonPill
      self ! PoisonPill
      context.stop(self)
    }
    case r:Report => {
      forwarder ! r
      manager ! r
    }
    case r:OilReport => {
//      forwarder ! r
      manager ! r
    }
    case t:Tick => router.route(Tick(),sender)
    case msg:Terminated => println("Ship " + sender.path.name + " stopped.")
    case c:ChangeRate => context.actorSelection(c.imoNumber) ! c
    case m:OneTimePoll => context.actorSelection(m.imoNumber) ! m
  }


}
