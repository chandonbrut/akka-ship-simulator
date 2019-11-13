package controllers

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import br.com.jityk.shipsimulator.actor.Protocol._
import br.com.jityk.shipsimulator.actor._
import com.vividsolutions.jts.geom.{LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import play.api.data.Form
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import play.api.data.Forms._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import play.api.libs.streams.ActorFlow

import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jferreira on 2/8/16.
  */

class SimulatorService @Inject() (implicit system:ActorSystem, materializer: Materializer, val controllerComponents: ControllerComponents) extends BaseController {

  val configForm = Form(
    mapping(
      "wktArea" -> text.verifying("Area WKT invalida", area => validateArea(area)),
      "imoFirstDigit" -> number(min = 0, max = 9),
      "numberOfShips" -> number(min = 0, max = 50000),
      "tickUnit" -> shortNumber(min = 0, max = 4),
      "simFrontEndBaseUrl" -> text)(Configuration.apply)(Configuration.unapply)
  )

  val oilConfigForm = Form(
    mapping(
      "wktArea" -> text.verifying("Area WKT invalida", area => validateArea(area)),
      "wktOilShape" -> text.verifying("Area WKT invalida", area => validateArea(area)),
      "oilId" -> text,
      "tickUnit" -> shortNumber(min = 0, max = 4))(OilConfiguration.apply)(OilConfiguration.unapply)
  )


  implicit val timeout = Timeout(2 seconds)


  val manager = system.actorOf(Props[ManagerActor],"managerActor")

  def validateArea(area:String) : Boolean = {
    val reader = new WKTReader
    try {
      val geometry = reader.read(area)
      geometry match {
        case p:Polygon => true
        case l:LineString => true
        case _ => false
      }
    } catch {
      case e:Exception => false
    }
  }

  def list() = Action.async {
    val runningConfigs = manager ? GetConfig()
    val simulations = runningConfigs.mapTo[List[SimulationStatus]]

    simulations.map(sims =>
      Ok(views.html.list(sims.toList))
    )
  }

  def poll(imoNumber:String) = Action {
    manager ! OneTimePoll(imoNumber)
    Ok("oneTimePoll:" + imoNumber)
  }
  def changeRate(imoNumber:String, rate:Int) = Action {
    manager ! ChangeRate(imoNumber,rate)
    Ok("changeRate:" + imoNumber + "\nrate:"+rate)
  }
  def stopSimulation(simulatorId:String) = Action {
    manager ! StopSimulation(simulatorId)
    Ok("Simulation stopped")
  }

  def prepare = Action { implicit request =>
    Ok(views.html.prepare(configForm))
  }

  def prepareOil = Action { implicit request =>
    Ok(views.html.oilprepare(oilConfigForm))
  }

  def configure = Action { implicit request =>
    configForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.prepare(formWithErrors))
      },
      configuration => {
        manager ! StartSimulation(configuration)
        Redirect(routes.SimulatorService.show())
      }
    )
  }

  def configureOil = Action { implicit request =>
    oilConfigForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.oilprepare(formWithErrors))
      },
      configuration => {
        manager ! StartOilSimulation(configuration)
        Redirect(routes.SimulatorService.show())
      }
    )
  }

  def configureJSON = Action(parse.json) {
    request => {
      val configuration = request.body.validate[Configuration]
      configuration.fold(
        errors => BadRequest(Json.obj("error" -> JsError.toJson(errors))),
        configuration => {
          manager ! StartSimulation(configuration)
          Ok(Json.obj("status" -> ("ok"),  "configuration" -> configuration))
        }
      )
    }
  }

  def show = Action.async { implicit request => {
      val url:String = routes.SimulatorService.wsMap().absoluteURL()
      val uri = if (request.secure) url.replace("https://","wss://")
                else url.replace("http://","ws://")

      val runningConfigs = manager ? GetConfig()
      val simulations = runningConfigs.mapTo[List[SimulationStatus]]

      simulations.map(sims =>
        Ok(views.html.show(uri, sims.map(p => p.config match {
          case c:Configuration => c.wktArea
          case c:OilConfiguration => c.wktArea
        }).toList))
      )
    }
  }

  def wsMap = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out,manager))
  }

}