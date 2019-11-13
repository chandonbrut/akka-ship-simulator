package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.vividsolutions.jts.algorithm.Angle
import com.vividsolutions.jts.geom.{Coordinate, Geometry, GeometryFactory}

import scala.util.Random

class OilActor(manager:ActorRef,oilId:String,restrictedArea:String, initialShape:String) extends Actor with ActorLogging {


  val myArea = GeoUtil.createPath(restrictedArea)
  var going = true;
  var currentPoint:Int = 0;
  val geometryFactory = new GeometryFactory();

  private var currentRate:Int = 15
  private var currentTick:Int = Random.nextInt(currentRate)
  private var currentPosition:Point = Point(myArea.getCoordinates()(0).y,myArea.getCoordinates()(0).x)

  private val SPEED = 5
  def THRESHOLD = SPEED*2/(60d)

  override def receive: Receive = {
    case t:Tick =>
      log.info(s"$self")
      println(self)
      val firstCoord = myArea.getCoordinates()(0)
      val myInitialShape:Geometry = GeoUtil.createShape(initialShape)
      val xOffset = myInitialShape.getCentroid.getCoordinate.x - firstCoord.x
      val yOffset = myInitialShape.getCentroid.getCoordinate.y - firstCoord.y
      context.become(
        online(
          GeoUtil.movePoints(myInitialShape,new Coordinate(-xOffset,-yOffset)),
          myArea,
          0,
          50,
        )
    )
  }




  def online(shape:Geometry, path:Geometry, currentTick:Int, currentRate:Int):Receive = {
    case g:GetPosition => {
      println("oil actor got poll")
      sender() ! PollReply(OilReport(oilId, shape.toText, scala.compat.Platform.currentTime))
    }

    case t:Tick => {

      updatePositionFollowingLine

      val moveOffset = new Coordinate(
        currentPosition.longitude - shape.getCentroid.getCoordinate.x,
        currentPosition.latitude - shape.getCentroid.getCoordinate.y
      )

      val scaledGeometry = GeoUtil.scaleGeometry(shape,(Random.nextDouble()+0.4))

      // rescale & move shape
      val newShape = GeoUtil.movePoints(
        shape,
        moveOffset
      )

      (currentTick % currentRate) match {
        case 0 => manager ! OilReport(oilId, newShape.toText, scala.compat.Platform.currentTime)
        case _ =>
      }
      context.become(online(newShape,path,currentTick+1, currentRate))
    }
  }




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
    val dx = (SPEED/60d)*Math.cos(angle)
    val dy = (SPEED/60d)*Math.sin(angle)

    currentPosition = Point(currentPosition.latitude + dy,currentPosition.longitude + dx)


  }

}