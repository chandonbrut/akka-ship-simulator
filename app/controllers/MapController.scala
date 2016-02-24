package controllers

import akka.actor.ActorSystem
import br.com.jityk.shipsimulator.actor.WebSocketActor
import br.com.jityk.shipsimulator.rest.SimulatorService
import play.api.mvc.{Action, WebSocket, Controller}
import play.api.Play.current

/**
  * Created by jonasferreira on 2/23/16.
  */
object MapController extends Controller {

  private val system = ActorSystem("simulator")

  def map = Action {
    Ok(views.html.map())
  }

  def wsMap = WebSocket.acceptWithActor[String, String] { request => out =>
    WebSocketActor.props(out,SimulatorService.simulation)
  }

  def testWs = Action { implicit request =>
    {
      val url:String = routes.MapController.wsMap().absoluteURL()
      val uri = url.replace("http://","ws://")
      Ok(views.html.ws(uri))
    }
  }

}
