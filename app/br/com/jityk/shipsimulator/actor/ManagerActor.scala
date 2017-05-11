package br.com.jityk.shipsimulator.actor

import akka.actor._
import akka.routing.{BroadcastRoutingLogic, Router, ActorRefRoutee}
import com.vividsolutions.jts.geom.{LineString, Polygon, GeometryFactory}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder

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
  val myRandomPositionGenerator = new RandomPointsBuilder()


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
      self ! PoisonPill
      context.stop(self)
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
