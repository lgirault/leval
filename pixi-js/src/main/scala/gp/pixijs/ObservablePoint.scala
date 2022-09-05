package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.ObservablePoint")
class ObservablePoint(cb: js.Function,
                      scope: js.Object,
                      x0: Double,
                      y0: Double)
  extends js.Object {

  var x : Double = js.native
  var y : Double = js.native

  def set(x: Double) : this.type = js.native
  def set(x: Double, y: Double) : this.type = js.native
}
