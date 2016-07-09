package leval.gui.gameScreen

import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Text, TextAlignment, TextFlow}

/**
  * Created by lorilan on 7/9/16.
  */
package object being {

  val switchUrl = this.getClass.getResource("/switch-icon.png").toExternalForm
  val switch = new Image(switchUrl)
  def switchImage(width : Double) =
    new ImageView(switch) {
      preserveRatio = true
      fitWidth = width
    }

  val eyeUrl = this.getClass.getResource("/eye-icon.png").toExternalForm
  val eye = new Image(eyeUrl)
  def eyeImage(width : Double) =
    new ImageView(eye) {
      preserveRatio = true
      fitWidth = width
    }

  def cardRectangle(txt : String, w : Double, h : Double) =
    new StackPane {
      val rect = Rectangle(w, h, Color.White)
      prefHeight = h
      rect.setStroke(Color.Green)
      rect.setArcWidth(20)
      rect.setArcHeight(20)
      val txtFlow = new TextFlow (new Text(txt)) {
        textAlignment = TextAlignment.Center
      }
      children = Seq(rect, txtFlow)
    }

  def okCanvas(cardWidth : Double) : Node = {
    val w = cardWidth / 4
    val cv = new Canvas(1.3*w, w+3)
    val gc = cv.graphicsContext2D
    //gc.fill = Color.White
    gc.stroke = Color.Green
    gc.strokeLine(1, 1, 1, w) // top -
    gc.strokeLine(1, 1, w, 1) // left |
    gc.strokeLine(w, 1, w, w) // right |
    gc.strokeLine(1, w, w, w) // bottom -

    gc.strokeLine(0, w/3, w/2, w) // \
    gc.strokeLine(w/2, w, 1.25*w, w/5) // /

    cv
  }

  def closeCanvas(cardWidth : Double) = {
    val w = cardWidth / 4
    val cv = new Canvas(w+3, w+3)
    val gc = cv.graphicsContext2D
    //gc.fill = Color.White
    gc.stroke = Color.Green
    gc.strokeLine(1, 1, 1, w) // top -
    gc.strokeLine(1, 1, w, 1) // left |
    gc.strokeLine(w, 1, w, w) // right |
    gc.strokeLine(1, w, w, w) // bottom -

    gc.strokeLine(1, 1, w, w) // \
    gc.strokeLine(w, 1, 1, w) // /

    cv
  }
}
