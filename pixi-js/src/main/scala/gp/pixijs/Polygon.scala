package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Polygon")
class Polygon(points0: js.Array[Point]) extends IHitArea {


  var closeStroke: Boolean = js.native

  var points: Array[Double] = js.native

  var `type`: Double = js.native

  override def clone() : Polygon = js.native

  def contains(x: Double, y: Double): Boolean = js.native
}
