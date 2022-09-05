package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.RoundedRectangle")
class RoundedRectangle(x0: Double = 0d,
                       y0: Double = 0d,
                       width0: Double = 0d,
                       height0: Double = 0d,
                       radius0: Double = 20d) extends js.Object {

  var x: Double = js.native
  var y: Double = js.native
  var width: Double = js.native
  var height: Double = js.native
  var radius: Double = js.native

  val `type`: Double = js.native

  override def clone() : RoundedRectangle = js.native

  def contains(x: Double, y: Double): Boolean = js.native

}
