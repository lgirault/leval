package leval.gui.gameScreen

import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.scene.Node
import scalafx.scene.effect.{Light, Lighting}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Region, StackPane}
import scalafx.util.Duration

/**
  * Created by lorilan on 6/29/16.
  */
class HighlightRegion(decorated : Node) extends StackPane {

  private val highlight = new Region {
    opacity = 0
    style = "-fx-border-width: 3; -fx-border-color: dodgerblue; -fx-background-color : rgba(12,56,100, 0.05);"
  }

  this.children = Seq(decorated, highlight)


  //  override val delegate: jfxsl.Region = new jfxsl.Region {
  //
  //    protected override def layoutChildren() {
  //      layoutInArea(highlight, 0, 0, getWidth, getHeight, getBaselineOffset, HPos.Center, VPos.Center)
  //    }
  //  }

  private val highlightTransition = new FadeTransition {
    node = highlight
    duration = Duration(200)
    fromValue = 0
    toValue = 1
  }

  //  style <== when(ReversiModel.legalMove(x, y)) choose
  //    "-fx-background-color: derive(dodgerblue, -60%)" otherwise
  //    "-fx-background-color: burlywood"

  val defaultEffect = effect

  onMouseEntered = (e: MouseEvent) => {
    //if (ReversiModel.legalMove(x, y).get) {
    highlightTransition.rate() = 1
    highlightTransition.play()

    effect = new Lighting {
      light = new Light.Distant {
        azimuth = -135
        elevation = 30
      }
    }
    //}
  }

  onMouseExited = (e: MouseEvent) => {
    highlightTransition.rate = -1
    highlightTransition.play()
    effect = null.asInstanceOf[javafx.scene.effect.Lighting]
  }

  //  onMouseClicked = (e: MouseEvent) => {
  //    ReversiModel.play(x, y)
  //    highlightTransition.rate() = -1
  //    highlightTransition.play()
  //  }
}