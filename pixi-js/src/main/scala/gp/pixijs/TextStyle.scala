package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal


@js.native
@JSGlobal("PIXI.TextStyle")
class TextStyle extends js.Object {

  //left, center or right
  var align: String = js.native
  var breakWords: Boolean = js.native
  var dropShadow: Boolean = js.native
  var dropShadowAlpha: Double = js.native
  var dropShadowAngle: Double = js.native
  var dropShadowBlur: Double = js.native
  var dropShadowColor: Double = js.native
  var dropShadowDistance: Double = js.native

  /*
  fill string | Array.<string> | number | Array.<number> | CanvasGradient | CanvasPattern
  A canvas fillstyle that will be used on the text e.g 'red', '#00FF00'. Can be an array to create a gradient eg ['#000000','#FFFFFF'] MDN
   */
  var fill: js.Any = js.native
  var fillGradientStops: Array[Double] = js.native
  var fillGradientType: Double = js.native
  var fontFamily: String = js.native
  var fontSize: Double = js.native
  var fontStyle: String = js.native
  var fontVariant: String = js.native
  var fontWeight: String = js.native

  var leading: Double = js.native
  var letterSpacing: Double = js.native
  var lineHeight: Double = js.native
  var lineJoin: String = js.native
  var miterLimit: Double = js.native
  var padding: Double = js.native
  var stroke: Double = js.native
  var strokeThickness: Double = js.native
  var textBaseline: String = js.native
  var trim: Boolean = js.native
  var whiteSpace: String = js.native
  var wordWrap: Boolean = js.native
  var wordWrapWidth: Double = js.native

  override def clone() : TextStyle = js.native
  def reset() : Unit = js.native
  def toFontString: String = js.native

}
