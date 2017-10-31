package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import com.vividsolutions.jts.geom.{GeometryFactory, LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by jferreira on 2/9/16.
  */
class SimulationActor  extends Actor {

  private var manager:ActorRef = null
  private var config:Configuration = Configuration(null,-1,-1,-1,null)

  var router:Router = Router(BroadcastRoutingLogic())
  val forwarder = context.actorOf(Props(new ForwarderActor()))
  val myRandomPositionGenerator = new RandomPointsBuilder()

  private def stopRouters() = {
    router.route(PoisonPill,self)
    router.removeRoutee(context.actorSelection("*"))
  }

  private def startTimers() = {
    val simulationTickRate = Protocol.duration(config.tickUnit)
    context.system.scheduler.schedule(0 seconds, simulationTickRate, self, Tick())
  }

  private def spawnShips(numberOfShips:Int,wktArea:String, cgCode:Int) = {
    myRandomPositionGenerator.setNumPoints(3)
    val base = cgCode * 1000000
    val children = (0 until numberOfShips).map(
      n => {
        val imoNumber = (base + n).toString
        val area = createPath(wktArea)
        println("Spawning ship " + imoNumber + " @ " + area)
        val child = context.actorOf(Props(new ShipActor(imoNumber, area, 1d, self)), imoNumber)
        context.watch(child)
        ActorRefRoutee(child)
      }
    )
    router = Router(BroadcastRoutingLogic(),children)
  }

  override def receive = {
    case msg:Configuration => {
      stopRouters()
      config = msg
      forwarder ! config
      manager = sender
      spawnShips(config.numberOfShips,config.wktArea,config.imoFirstDigit)
      startTimers()
    }
    case msg:Register => {
      manager forward msg
    }
    case msg:GetConfig => sender ! config
    case msg:OneTimePoll => manager ! msg
    case msg:ChangeRate => manager ! msg
    case msg:StopSimulation => {
      stopRouters()
      forwarder ! PoisonPill
      self ! PoisonPill
      context.stop(self)
    }
    case r:Report => {
      forwarder ! r
      manager ! r
    }
    case t:Tick => router.route(Tick(),sender)
    case msg:Terminated => println("Ship " + sender.path.name + " stopped.")
    case c:ChangeRate => context.actorSelection(c.imoNumber) ! c
    case m:OneTimePoll => context.actorSelection(m.imoNumber) ! m

  }

  def createPath(area:String):String = {
    val myArea = new WKTReader().read(area)
    myArea match {
      case p:Polygon => {
        myRandomPositionGenerator.setExtent(myArea)
        val gf = new GeometryFactory()
        val geo = gf.createLineString(myRandomPositionGenerator.getGeometry.getCoordinates)
        geo.toText
      }
      case l:LineString => myArea.toText
    }
  }

}
