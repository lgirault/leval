package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Ellipse")
class Ellipse(x0: Double = 0, y0: Double = 0,
              halfWidth: Double= 0, halfHeight:Double =0) extends js.Object {

  var height : Double = js.native
  var width : Double = js.native
  var x : Double = js.native
  var y : Double = js.native
  val `type` : Double = js.native

  override def clone() : Ellipse = js.native

  def contains(x: Double, y: Double): Boolean = js.native

  def getBounds() : Rectangle = js.native
}
