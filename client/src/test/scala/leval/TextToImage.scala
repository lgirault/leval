package leval

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Pos
import scalafx.scene.{Group, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.{ImageView, WritableImage}
import scalafx.scene.layout.{FlowPane, StackPane}

/**
  * Created by lorilan on 8/28/16.
  */
object TextToImage extends JFXApp {
  def cardRectangleImg(txt : String, w : Double, h : Double) = {
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
          "-fx-border-color: green;" +
          "-fx-border-insets: 2;" +
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
      children = new ImageView(cardRectangleImg(txt, w, h)){
        preserveRatio = true
        fitHeight = h
      }
    }


  stage = new PrimaryStage {
    title = "Le Val des Étoiles"
    scene = new Scene{
      root = new FlowPane{
         children add cardRectangle("Toto", 400d,400d)
      }
    }
  }
}
