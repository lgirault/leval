package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait LineStyle extends js.Any {

  var width: Double = js.native
  var color: Int = js.native
  var alpha: Double = js.native
  var alignment: Double = js.native
  var native: Boolean = js.native

}

object LineStyle{
  def apply(width: Double = 0,
            color: Int = 0x0,
            alpha: Double = 1,
            alignment: Double = 0.5,
            native: Boolean = false) : LineStyle = {
    js.Dynamic.literal(
      width = width,
      color = color,
      alpha = alpha,
      alignment = alignment,
      native = native
    ).asInstanceOf[LineStyle]
  }
}


@js.native
@JSGlobal("PIXI.Graphics")
class Graphics(geometry : GraphicsGeometry = null) extends Container {

  def beginFill(color: Int = 0, alpha: Double = 1) : Graphics = js.native
  def endFill() : Graphics = js.native
  def clear(): Graphics = js.native
  val line: LineStyle = js.native

  def lineStyle(options: LineStyle): Graphics = js.native

  def lineStyle(width: Double = 0,
                color: Int = 0x0,
                alpha: Double = 1,
                alignment: Double = 0.5,
                native: Boolean = false): Graphics = js.native

  def drawCircle(x: Double, y:Double, radius: Double) : Graphics = js.native
  def drawEllipse(x: Double, y:Double, width: Double, height: Double) : Graphics = js.native
  def drawPolygon(path: js.Array[Point]): Graphics = js.native
  def drawPolygon(path: Polygon): Graphics = js.native
  def drawRect(x : Double, y: Double, width: Double, height: Double) : Graphics =  js.native
  def drawRoundedRect(x : Double, y: Double, width: Double, height: Double, radius: Double) : Graphics =  js.native
  def drawShape(shape: Circle) : Graphics =  js.native
  def drawShape(shape: Ellipse) : Graphics =  js.native
  def drawShape(shape: Polygon) : Graphics =  js.native
  def drawShape(shape: Rectangle) : Graphics =  js.native
  def drawShape(shape: RoundedRectangle) : Graphics =  js.native
  def drawStar(x : Double, y: Double, points: Int, radius: Double, innerRadius: Double, rotation: Double = 0) : Graphics =  js.native

}
