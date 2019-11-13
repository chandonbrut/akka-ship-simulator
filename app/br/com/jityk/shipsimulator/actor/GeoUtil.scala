package br.com.jityk.shipsimulator.actor

import com.vividsolutions.jts.geom.{Coordinate, Geometry, GeometryFactory, LineString, Polygon}
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.shape.random.RandomPointsBuilder

object GeoUtil {

  val geometryFactory = new GeometryFactory()

  def createCircle(center:Coordinate,size:Double) = {
    geometryFactory.createPoint(center).buffer(size)
  }

  def movePoints(shape:Geometry, moveAmount:Coordinate): Geometry = {
    val points = shape.getCoordinates
    val movedPoints = for (point <- points) yield new Coordinate(point.x + moveAmount.x, point.y + moveAmount.y)
    geometryFactory.createPolygon(movedPoints)
  }

  def scaleGeometry(shape:Geometry, ratio:Double):Geometry = {
    shape.buffer(ratio)
  }


  def createPath(area:String):Geometry = {

    val myRandomPositionGenerator = new RandomPointsBuilder()
    myRandomPositionGenerator.setNumPoints(3)

    val myArea = new WKTReader().read(area)
    myArea match {
      case p:Polygon => {
        myRandomPositionGenerator.setExtent(myArea)
        val gf = new GeometryFactory()
        gf.createLineString(myRandomPositionGenerator.getGeometry.getCoordinates)
      }
      case l:LineString => l
    }
  }

  def createShape(area:String):Geometry = {

    val myArea = new WKTReader().read(area)
    myArea match {
      case p:Polygon => p
      case l:LineString => l
    }
  }

  def createRandomPointInside(area:Geometry):com.vividsolutions.jts.geom.Point = {
    val myRandomPositionGenerator = new RandomPointsBuilder()
    myRandomPositionGenerator.setNumPoints(1)
    myRandomPositionGenerator.getGeometry.getCentroid
  }

  def point(position:Point):Geometry = {
    geometryFactory.createPoint(new Coordinate(position.longitude,position.latitude))
  }

  def point(position:OilReport):Geometry = {
    createShape(position.shape)
  }

  def point(position:Report):Geometry = {
    geometryFactory.createPoint(new Coordinate(position.position.longitude,position.position.latitude))
  }
}
