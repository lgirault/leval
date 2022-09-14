package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Sprite")
class Sprite(texture0: Texture) extends Container {

  val anchor : ObservablePoint = js.native

  var texture: Texture = js.native
}

object Sprite {

  extension (s: Sprite)
    def scaleForWidth(w: Double) = 
      s.width = w
      val Point(x,_) = s.scale
      s.scale = new Point(x,x)
      
}