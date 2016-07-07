package leval.gui.gameScreen

import leval.core._

import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
  * Created by Loïc Girault on 06/07/16.
  */

object StarPanel {
  def apply(ogame : ObservableGame,
    numStar : Int,
    controller : GameScreenControl) : StarPanel = {
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
  extends CardDropTarget(new VBox {

  spacing = 10
  alignment = Pos.Center
  children = Seq(
    new Label(starName),
    new Label("Majesty"),
    majestyValueLabel
  )
}){

  def onDrop(c: Card, origin: Origin): Unit = {
    controller.directEffect(c, origin)
  }

}
