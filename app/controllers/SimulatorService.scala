package controllers

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import br.com.jityk.shipsimulator.actor.Protocol._
import br.com.jityk.shipsimulator.actor._
import com.vividsolutions.jts.geom.{LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import play.api.data.Form
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.data.Forms._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import play.api.Play.current

/**
  * Created by jferreira on 2/8/16.
  */

object SimulatorService extends Controller {

  val configForm = Form(
    mapping(
      "wktArea" -> text.verifying("Area WKT invalida", area => validateArea(area)),
      "imoFirstDigit" -> number(min = 0, max = 9),
      "numberOfShips" -> number(min = 0, max = 10000),
      "tickUnit" -> shortNumber(min = 0, max = 4),
      "simFrontEndBaseUrl" -> text)(Configuration.apply)(Configuration.unapply)
  )

  private val system = ActorSystem("simulator")

  implicit val timeout = Timeout(2 seconds)


  val simulation = system.actorOf(Props[SimulationActor])

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

  def getRunningConfig() =  {
    val runningConfig:Future[Configuration] = (simulation ? GetConfig()).mapTo[Configuration]
    val c= Await.result(runningConfig,2 seconds)
    c
  }

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

  def prepare = Action { implicit request =>
    Ok(views.html.prepare(configForm))
  }

  def configure = Action { implicit request =>
    configForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.prepare(formWithErrors))
      },
      conf => {
        simulation ! conf
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
          simulation ! configuration
          Ok(Json.obj("status" -> ("ok"),  "configuration" -> configuration))
        }
      )
    }
  }

  def show = Action { implicit request => {

      val url:String = routes.SimulatorService.wsMap().absoluteURL()
      val uri = url match {
        case u:String if u.matches("""http://.+""") => u.replace("http://","ws://")
        case u:String if u.matches("""https://.+""") =>u.replace("https://","wss://")
      }

      val runningConfig = getRunningConfig()
      Ok(views.html.show(uri, runningConfig.wktArea))
    }
  }

  def wsMap = WebSocket.acceptWithActor[String, String] { request => out =>
    WebSocketActor.props(out,simulation)
  }

}