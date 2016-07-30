package leval.gui.gameScreen

import leval.core.CardOrigin
import leval.gui.text.ValText

import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text

/**
  * Created by Loïc Girault on 06/07/16.
  */

object StarPanel {
  def apply(ogame : ObservableGame,
    numStar : Int,
    controller : GameScreenControl)
   (implicit txt : ValText): StarPanel = {
    val star = ogame.stars(numStar)

    new StarPanel(numStar, controller,
      star.id.name, new Label(star.majesty.toString))
  }
}

class StarPanel
( val numStar : Int,
  controller : GameScreenControl,
  val starName : String,
  val majestyValueLabel : Label = new Label())
(implicit txt : ValText)
  extends CardDropTarget(new VBox {
  spacing = 10
  alignment = Pos.Center
  children = Seq(
  new Text(starName),
  new Text(txt.majesty),
  majestyValueLabel
  )
}) {

  def onDrop(origin: CardOrigin): Unit =
    controller.directEffect(origin)

}
