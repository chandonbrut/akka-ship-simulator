package br.com.jityk.shipsimulator.actor

import play.api.libs.json.Json

import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.concurrent.duration._

/**
  * Created by jferreira on 2/8/16.
  */

case class StartSimulation(duration:FiniteDuration)
case class StopSimulation()
case class Point(latitude:Double, longitude:Double)
case class ChangeRate(imoNumber:String, rate:Int)
case class OneTimePoll(imoNumber:String)
case class Tick()
case class Report(imoNumber: String, position:Point, timestamp:Long)
case class SpawnShips(numberOfShips:Int,cgCode:Int,area:String)
case class Configuration(
                      wktArea:String,
                      imoFirstDigit:Int,
                      numberOfShips:Int,
                      tickUnit:Short,
                      simFrontEndBaseUrl:String)

object Protocol {

  implicit val configureReads = Json.reads[Configuration]
  implicit val configureWrites = Json.writes[Configuration]

  def duration(tickUnit:Int):FiniteDuration = tickUnit match {
    case 0 => 1 millisecond
    case 1 => 1 second
    case 2 => 1 minute
    case 3 => 1 hour
    case 4 => 1 day
    case _ => 1 minute
  }

}