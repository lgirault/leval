package gp.pixijs

//import typings.offscreencanvas.HTMLCanvasElement
import org.scalajs.dom.HTMLCanvasElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Application")
class Application(options: js.Object) extends js.Object:

  val view : HTMLCanvasElement = js.native

  val screen: Rectangle = js.native

  var stage: Container = js.native

  var ticker: Ticker = js.native


object Application:

  def apply(width: Int, height: Int, backgroundColor: Int = 0x000000) : Application = 
    new Application(options(width, height, backgroundColor))
    
  def options(width: Int, height: Int, backgroundColor: Int = 0x000000) : js.Object = {
    js.Dynamic.literal(width = width, height = height, backgroundColor = backgroundColor)
  }
