package br.com.jityk.shipsimulator.actor

import java.util.{MissingResourceException, ResourceBundle}

/**
  * Created by jferreira on 2/8/16.
  */
object ShipSimulatorBundle {

  def WRONG_REQUEST_TYPE = get("error.request.type.wrong")

  val myBundle:ResourceBundle = ResourceBundle.getBundle("ShipSimulator")

  def get(key:String) : String = {
    require(key.nonEmpty)
    try {
      myBundle.getString(key)
    } catch {
      case ex:MissingResourceException => {key}
    }
  }
}
