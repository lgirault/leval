package gp.pixijs

import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Text")
class Text(text: String,
           style : js.UndefOr[TextStyle] = (),
           canvas: js.UndefOr[HTMLCanvasElement] = ()) extends Sprite(???) {

}
