package controllers

import java.sql.Timestamp
import br.com.jityk.shipsimulator.actor.Report
import com.google.inject.{Inject, Singleton}
import play.api.db.Database

@Singleton
class  DBWriter @Inject() (db:Database)  {

  def writeReport(report:Report): Unit = {
    db.withConnection {
      conn =>
        val statement = conn.prepareStatement(
          s"""
            INSERT INTO report(imo,ts,latitude,longitude) VALUES (?,?,?,?);
          """
        )

        statement.setString(1,report.imoNumber)
        statement.setTimestamp(2,new Timestamp(report.timestamp))
        statement.setDouble(3,report.position.latitude)
        statement.setDouble(4,report.position.longitude)

        statement.executeUpdate()
    }

  }
}