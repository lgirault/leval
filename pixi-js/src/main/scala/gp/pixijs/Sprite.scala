package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Sprite")
class Sprite(texture0: Texture) extends Container {

  val anchor : ObservablePoint = js.native

  var texture: Texture = js.native
}
