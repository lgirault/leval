package gp.pixijs

import org.scalajs.dom.{HTMLCanvasElement, HTMLImageElement, HTMLVideoElement}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Texture")
class Texture(baseTexture0: BaseTexture,
              frame0: js.UndefOr[Rectangle] = (),
              orig0: js.UndefOr[Rectangle] = (),
              trim0: js.UndefOr[Rectangle] = (),
              rotate0: js.UndefOr[Double] = (),
              anchor: js.UndefOr[Point] = (),

             )
  extends EventEmitter {

  val height: Int = js.native
  val width: Int = js.native

  var frame: Rectangle = js.native
  var rotate: Double = js.native

  var orig: Rectangle = js.native
  var trim: Rectangle = js.native

  var  defaultAnchor: Point = js.native

  var baseTexture: BaseTexture = js.native

}

@js.native
@JSGlobal("PIXI.Texture")
object Texture extends js.Object {
  def from( source: String | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement  ): Texture = js.native
}
