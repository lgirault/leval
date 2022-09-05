package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Point")
class Point(x : Double, y: Double) extends IPoint

object Point {

  implicit def dpair2point(pair: (Double, Double)) : Point = new Point(pair._1, pair._2)
  implicit def ipair2point(pair: (Int, Int)) : Point = new Point(pair._1, pair._2)
  implicit def idpair2point(pair: (Int, Double)) : Point = new Point(pair._1, pair._2)
  implicit def dipair2point(pair: (Double, Int)) : Point = new Point(pair._1, pair._2)
}
