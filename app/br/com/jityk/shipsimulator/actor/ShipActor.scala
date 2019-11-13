package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorRef}
import com.vividsolutions.jts.algorithm.Angle
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder

import scala.util.Random

/**
  * Created by jferreira on 2/8/16.
  */
class ShipActor(imoNumber:String, restrictedArea:String, speed:Double, manager:ActorRef) extends Actor {
  val myArea = GeoUtil.createPath(restrictedArea)
  var going = true;
  var currentPoint:Int = 0;
  val geometryFactory = new GeometryFactory();

  private var currentRate:Int = 50
  private var currentTick:Int = Random.nextInt(currentRate)
  private var currentPosition:Point = Point(myArea.getCoordinates()(0).y,myArea.getCoordinates()(0).x)



  override def receive = {
    case c:ChangeRate => changeRate(c.rate)
    case p:OneTimePoll => reportMyPosition(manager)
    case t:Tick => tick
    case g:GetPosition => {
      println("ship got request")
      sender() ! ShipPollReply(Report(imoNumber,currentPosition,scala.compat.Platform.currentTime))
    }
  }

  def changeRate(newRate:Int): Unit = {
    require(newRate >= 0)
    currentRate = newRate
    currentTick = currentTick + Random.nextInt(currentRate)
  }

  def THRESHOLD = speed*2/(60d)


  def updatePositionFollowingLine = {
    val myPath = myArea.getCoordinates
    var angle = 0d
    val rightNow = geometryFactory.createPoint(new Coordinate(currentPosition.longitude,currentPosition.latitude))

    if (going) {
      if (currentPoint == (myPath.length-1)) {
        going = false
        angle = Angle.angle(myPath(currentPoint),myPath(currentPoint-1))
      } else {
        val dist = rightNow.distance(geometryFactory.createPoint(myPath(currentPoint+1)))
        if (dist < THRESHOLD) {
          currentPoint = currentPoint+1
          currentPosition = Point(myPath(currentPoint).y,myPath(currentPoint).x)
        } else {
          angle = Angle.angle(myPath(currentPoint),myPath(currentPoint+1))
        }
      }
    } else {

      if (currentPoint == 0) {
        going = true
        angle = Angle.angle(myPath(currentPoint),myPath(currentPoint+1))
      } else {
        val dist = rightNow.distance(geometryFactory.createPoint(myPath(currentPoint-1)))
        if (dist < THRESHOLD) {
          currentPoint = currentPoint-1
          currentPosition = Point(myPath(currentPoint).y,myPath(currentPoint).x)
        } else {
          angle = Angle.angle(myPath(currentPoint),myPath(currentPoint-1))
        }
      }
    }
    val dx = (speed/60d)*Math.cos(angle)
    val dy = (speed/60d)*Math.sin(angle)

    currentPosition = Point(currentPosition.latitude + dy,currentPosition.longitude + dx)
  }

  def updatePosition = {
    myArea match {
      case p:Polygon => {
        val p = GeoUtil.createRandomPointInside(myArea)
        currentPosition = Point(p.getY,p.getX)
      }
      case l:LineString =>updatePositionFollowingLine
    }
  }


  def justLayThere = {
    val shouldPrint = false
    if(shouldPrint)println("Ship " + imoNumber + " standing at lat=" + currentPosition.latitude + " lon=" + currentPosition.longitude)
  }
  def reportMyPosition(interestedParty:ActorRef) = interestedParty ! Report(imoNumber,currentPosition,scala.compat.Platform.currentTime)

  def tick = {
    currentTick = currentTick + 1
    updatePosition
    (currentTick % currentRate) match {
      case 0 => reportMyPosition(manager)
      case _ => justLayThere
    }
  }



}
