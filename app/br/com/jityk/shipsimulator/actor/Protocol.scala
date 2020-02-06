package br.com.jityk.shipsimulator.actor

import akka.actor.ActorRef
import com.vividsolutions.jts.geom.Geometry
import play.api.libs.json.Json

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.duration._

/**
  * Created by jferreira on 2/8/16.
  */

case class JSONReport(imoNumber:String,timestamp:Long,lat:Double,lon:Double)
case class Register()
case class StartSimulation(configuration:Configuration)
case class StartOilSimulation(configuration:OilConfiguration)
case class GetConfig()
case class GetPosition()
case class StopSimulation(simulatorId:String)
case class Point(latitude:Double, longitude:Double)
case class ChangeRate(imoNumber:String, rate:Int)
case class OneTimePoll(imoNumber:String)
case class Tick()
case class OilReport(oilId:String,shape:String,timestamp:Long)
case class Report(imoNumber: String, position:Point, timestamp:Long)
case class SpawnShips(numberOfShips:Int,cgCode:Int,area:String)
case class SimulationStatus(simulatorId:String, config:Configuration)
case class Configuration(
                      wktArea:String,
                      imoFirstDigit:Int,
                      numberOfShips:Int,
                      tickUnit:Short,
                      simFrontEndBaseUrl:String)

case class OilConfiguration(
                          wktArea:String,
                          wktOilShape:String,
                          oilId:String,
                          tickUnit:Short
                          )


case class OilPoll()
case class PollReply(report:OilReport)
case class ShipPollReply(report:Report)


object Protocol {

  implicit val configureReads = Json.reads[Configuration]
  implicit val configureWrites = Json.writes[Configuration]

  implicit val oilCnfigureReads = Json.reads[OilConfiguration]
  implicit val oilConfigureWrites = Json.writes[OilConfiguration]

  def duration(tickUnit:Int):FiniteDuration = tickUnit match {
    case 0 => 1 millisecond
    case 1 => 1 second
    case 2 => 1 minute
    case 3 => 1 hour
    case 4 => 1 day
    case _ => 1 minute
  }

}