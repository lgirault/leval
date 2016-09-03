package leval.gui.gameScreen

import scalafx.geometry.Pos
import scalafx.scene.{Group, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.layout.StackPane

/**
  * Created by lorilan on 7/9/16.
  */
package object being {

  val switchUrl = this.getClass.getResource("/icons/switch.png").toExternalForm
  val switch = new Image(switchUrl)
  def switchImage(width : Double) =
    new ImageView(switch) {
      preserveRatio = true
      fitWidth = width
    }

  val eyeUrl = this.getClass.getResource("/icons/eye.png").toExternalForm
  val eye = new Image(eyeUrl)
  def eyeImage(width : Double) =
    new ImageView(eye) {
      preserveRatio = true
      fitWidth = width
    }

  val educateUrl = this.getClass.getResource("/icons/educate.png").toExternalForm
  val educate = new Image(educateUrl)
  def educateImage(width : Double) =
    new ImageView(educate) {
      preserveRatio = true
      fitWidth = width
    }

  val okUrl = this.getClass.getResource("/icons/ok.png").toExternalForm
  val ok = new Image(okUrl)
  def okImage(width : Double) =
    new ImageView(ok) {
      preserveRatio = true
      fitWidth = width
    }

  val cancelUrl = this.getClass.getResource("/icons/cancel.png").toExternalForm
  val cancel = new Image(cancelUrl)
  def cancelImage(width : Double) =
    new ImageView(cancel) {
      preserveRatio = true
      fitWidth = width
    }

  def cardRectangleImg(txt : String) = {
    val w = CardImg.width
    val h = CardImg.height
    val label = new Label(txt) {
      minWidth = w
      minHeight = h
      maxWidth = w
      maxHeight = h
      prefWidth = w
      prefHeight = h
      alignment = Pos.Center
      style =
          "-fx-background-color: white;" +
          "-fx-background-radius: 20;" +
          "-fx-border-color: green;" +
          "-fx-border-width: 3;" +
          "-fx-border-radius: 20;" +
          "-fx-font-size: 36;" +
          "-fx-text-alignment: center;"

      wrapText = true
    }
    val scene = new Scene(new Group(label))
    val img = new WritableImage(w.toInt, h.toInt)
    scene.snapshot(img)
    img
  }

  def cardRectangle(txt : String, w : Double, h : Double) =
    new StackPane {
      prefHeight = h
      children = new ImageView(cardRectangleImg(txt)){
        preserveRatio = true
        fitHeight = h
      }
    }
}
