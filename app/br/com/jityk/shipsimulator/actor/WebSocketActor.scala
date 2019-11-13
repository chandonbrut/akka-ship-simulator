package br.com.jityk.shipsimulator.actor

import akka.actor._
import play.api.libs.json.{JsObject, Json}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by jonasferreira on 2/23/16.
  */
object WebSocketActor {
  def props(out: ActorRef, manager:ActorRef) = Props(new WebSocketActor(out, manager))
}

class WebSocketActor(out:ActorRef, manager:ActorRef) extends Actor {


  implicit val pointReads = Json.reads[Point]
  implicit val pointWrites = Json.writes[Point]

  implicit val reportReads = Json.reads[Report]
  implicit val reportWrites = Json.writes[Report]


  implicit val oilReportReads = Json.reads[OilReport]
  implicit val oilReportWrites = Json.writes[OilReport]


  def receive = {
    case "register" => {
      manager ! Register()
    }
    case "poll" => {
      implicit val timeout:Timeout = Timeout(3 seconds)
      val answer = (context.actorOf(Props[OilPollActor]) ? OilPoll()).mapTo[Report]
      answer.onComplete {
        case Success(report) =>  {
          val jsonReport = Json.toJson(report)
          out ! (jsonReport.as[JsObject] + ("pollResult" -> Json.toJson(true))).toString()
        }
        case Failure(e) =>
      }
    }
    case msg:Report => out ! Json.toJson(msg).toString()
    case msg:OilReport => out ! Json.toJson(msg).toString()
  }

}