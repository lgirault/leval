package leval.gui.gameScreen

import leval.core.CardOrigin
import leval.gui.text.ValText

import scalafx.geometry.Pos
import scalafx.scene.{Group, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.{ImageView, WritableImage}
import scalafx.scene.layout._

/**
  * Created by Loïc Girault on 06/07/16.
  */

object StarPanel {
  def apply(controller : GameScreenControl,
            width : Double,
            numStar : Int)
           (implicit txt : ValText): StarPanel = {
    val star = controller.game.stars(numStar)

    new StarPanel(controller, width, numStar , star.id.name)
  }

  def image
  ( starName : String,
    majesty : Int)(implicit txt : ValText) = {
    val w = CardImg.width * 1.5
    val h = CardImg.height

    val text = s"$starName\n${txt.majesty}\n$majesty"

    val label = new Label(text) {
      minWidth = w
      minHeight = h
      maxWidth = w
      maxHeight = h
      prefWidth = w
      prefHeight = h
      alignment = Pos.Center
      style =
        "-fx-border-insets: 2;" +
        "-fx-font-size: 40;" +
        "-fx-text-alignment: center;"
    }
    val scene = new Scene(new Group(label))
    val img = new WritableImage(w.toInt, h.toInt)
    scene.snapshot(img)
    img
  }


}

class StarPanel
( controller : GameScreenControl,
  width : Double,
  val numStar : Int,
  val starName : String,
  val wrapper : BorderPane = new BorderPane())
(implicit txt : ValText)
  extends CardDropTarget(wrapper) {
  def majesty : Int = controller.game.stars(numStar).majesty

  def update() = {
    wrapper.children.clear()
    val img = StarPanel.image(starName, majesty)
    val imgview = new ImageView(img){
      preserveRatio = true
      fitWidth = width
    }
    wrapper.center = imgview
  }

  update()

  def onDrop(origin: CardOrigin): Unit =
    controller.directEffect(origin)

}
