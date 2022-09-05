package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Container")
class Ticker extends js.Object {

  def add(fn: js.Function,
          context: js.Any = 0,
          priority : Double = UpdatePriority.NORMAL): Ticker = js.native
}
