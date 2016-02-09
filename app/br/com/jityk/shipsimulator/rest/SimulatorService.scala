package br.com.jityk.shipsimulator.rest

import akka.actor.{PoisonPill, Props, ActorSystem}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import br.com.jityk.shipsimulator.actor._
import br.com.jityk.shipsimulator.actor.Protocol._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by jferreira on 2/8/16.
  */

object SimulatorService extends Controller {

  private val system = ActorSystem("simulator")
  private val simulation = system.actorOf(Props[SimulationActor])

  def poll(imoNumber:String) = Action {
    simulation ! OneTimePoll(imoNumber)
    Ok("oneTimePoll:" + imoNumber)
  }
  def changeRate(imoNumber:String, rate:Int) = Action {
    simulation ! ChangeRate(imoNumber,rate)
    Ok("changeRate:" + imoNumber + "\nrate:"+rate)
  }

  def stopSimulation() = Action {
    simulation ! StopSimulation()
    Ok("Simulation stopped")
  }

  def configure = Action(parse.json) {
    request => {
      val configuration = request.body.validate[Configuration]
      configuration.fold(
        errors => BadRequest(Json.obj("error" -> JsError.toJson(errors))),
        configuration => {
          simulation ! configuration
          Ok(Json.obj("status" -> ("ok"),  "configuration" -> configuration))
        }
      )
    }
  }

}