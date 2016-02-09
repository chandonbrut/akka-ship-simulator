package br.com.jityk.shipsimulator.actor

import akka.actor.{ActorRef, Actor}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder

/**
  * Created by jferreira on 2/8/16.
  */
class ShipActor(imoNumber:String, restrictedArea:String, manager:ActorRef) extends Actor {

  val myArea = new WKTReader().read(restrictedArea)
  val myRandomPositionGenerator = new RandomPointsBuilder()

  private var currentRate:Int = 1
  private var currentTick:Int = 0
  private var currentPosition:Point = Point(0,0)

  override def receive = {
    case c:ChangeRate => changeRate(c.rate)
    case p:OneTimePoll => {
      val senderRef = sender
      reportMyPosition(senderRef)
    }
    case t:Tick => tick
  }

  def changeRate(newRate:Int): Unit = {
    require(newRate >= 0)
    currentRate = newRate
  }

  def updatePosition = {
    myRandomPositionGenerator.setExtent(myArea)
    myRandomPositionGenerator.setNumPoints(1)
    val newPosition = myRandomPositionGenerator.getGeometry
    currentPosition = Point(newPosition.getCentroid.getY,newPosition.getCentroid.getX)
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
