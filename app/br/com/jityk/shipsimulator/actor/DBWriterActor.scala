package br.com.jityk.shipsimulator.actor

import akka.actor.{Actor, ActorLogging}
import controllers.DBWriter

class  DBWriterActor (writer:DBWriter)  extends Actor with ActorLogging {

  override def receive: Receive = {
    case r:Report => writer.writeReport(r)
  }

}
