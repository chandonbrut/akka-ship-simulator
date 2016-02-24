import br.com.jityk.shipsimulator.actor.WebSocketActor
import play.api.{Application, GlobalSettings, Logger}
import play.api.mvc._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    super.onStart(app)
  }

  override def onStop(app: Application) {
    Logger.info("Application has stopped")
    super.onStop(app)
  }

}