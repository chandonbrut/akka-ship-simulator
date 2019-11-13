package br.com.jityk.shipsimulator.actor

import com.vividsolutions.jts.geom.Coordinate

object GeoTest extends App {

  val circle = GeoUtil.createCircle(new Coordinate(0,0),5)
  val movedCircle = GeoUtil.movePoints(circle,new Coordinate(1,1))

  println(circle.toText)
  println(movedCircle.toText)

}
