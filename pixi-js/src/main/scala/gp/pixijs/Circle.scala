package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Circle")
class Circle(x0: Double = 0, y0: Double = 0, radius0: Double= 0) extends js.Object {

  var radius : Double = js.native
  var x : Double = js.native
  var y : Double = js.native
  val `type` : Double = js.native

  override def clone() : Circle = js.native

  def contains(x: Double, y: Double): Boolean = js.native

  def getBounds() : Rectangle = js.native
}
