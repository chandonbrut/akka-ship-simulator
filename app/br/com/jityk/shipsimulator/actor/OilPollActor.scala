package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ReceiveTimeout}
import akka.util.Timeout
import com.vividsolutions.jts.geom.Geometry

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

class OilPollActor extends Actor with ActorLogging {

  def distance(position:Point, report:Report):Double = {
    val oilPoint:Geometry = GeoUtil.point(position)
    val shipPoint:Geometry = GeoUtil.point(report)
    oilPoint.distance(shipPoint)
  }

  def distance(position:OilReport, report:Report):Double = {
    val oilShape:Geometry = GeoUtil.point(position)
    val shipPoint:Geometry = GeoUtil.point(report)
    oilShape.distance(shipPoint)
  }


  override def receive: Receive = {
    case p:OilPoll => {
      val answerTo = sender
      implicit val timeout:Timeout = Timeout(3 seconds)
      val oilActor = context.actorSelection("/user/managerActor/*/oilActor")

      val report = (oilActor ? GetPosition()).mapTo[PollReply]

      report.onComplete {
        case Success(r) => {

          val children = context.actorSelection("/user/managerActor/*/shipActor-*")

          val positions = children ! GetPosition()

          context.setReceiveTimeout(1 second)
          context.become(waitingForReplies(answerTo,List(),r.report))

        }
        case Failure(e) => println(e)
      }
    }
  }


  def waitingForReplies(answerTo:ActorRef, reports:List[Report], oilReport:OilReport): Receive = {
    case ShipPollReply(report) => {
      val newReports = report :: reports
      context.setReceiveTimeout(1 second)
      context.become(waitingForReplies(answerTo,newReports,oilReport))
    }
    case ReceiveTimeout => {
      val report = reports.minBy(p => distance(oilReport,p))
      answerTo ! report
      context stop self
    }
  }

}
