package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Rectangle")
class Rectangle(x0: Double = 0d,
                y0: Double = 0d,
                width0: Double = 0d,
                height0: Double = 0d) extends js.Object {

  var x: Double = js.native
  var y: Double = js.native
  var width: Double = js.native
  var height: Double = js.native
  var left: Double = js.native
  var right: Double = js.native
  var top: Double = js.native

  val `type`: Double = js.native

  override def clone() : Rectangle = js.native

  def contains(x: Double, y: Double): Boolean = js.native

  def copyFrom(rectangle: Rectangle): Rectangle = js.native
  def copyTo(rectangle: Rectangle): Rectangle = js.native
  def enlarge(rectangle: Rectangle): Rectangle = js.native
  def fit(rectangle: Rectangle): Rectangle = js.native

  def pad(paddingX: Double= 0, paddingY: Double): Rectangle = js.native
}
