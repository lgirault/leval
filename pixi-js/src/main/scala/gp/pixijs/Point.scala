package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Point")
class Point(x : Double, y: Double) extends IPoint

object Point {

  def unapply(p: IPoint): Some[(Double, Double)] = 
    Some((p.x, p.y))

  given Conversion[(Double, Double), Point] = pair => new Point(pair._1, pair._2)
  given Conversion[(Int, Int), Point] = pair => new Point(pair._1, pair._2)
  given Conversion[(Int, Double), Point] = pair => new Point(pair._1, pair._2)
  given Conversion[(Double, Int), Point] = pair => new Point(pair._1, pair._2)
  
}
