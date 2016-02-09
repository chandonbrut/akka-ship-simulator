import play.api.{Application, GlobalSettings, Logger}

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