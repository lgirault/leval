package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Container")
abstract class DisplayObject extends EventEmitter {

  var x : Double = js.native
  var y : Double = js.native
  var zIndex : Double = js.native

  var pivot: IPoint = js.native
  var rotation: Double = js.native
  var scale: IPoint = js.native
  var interactive: Boolean = js.native
  var buttonMode : Boolean = js.native
  var hitArea : IHitArea = js.native

  def updateTransform(): Unit = js.native
}
