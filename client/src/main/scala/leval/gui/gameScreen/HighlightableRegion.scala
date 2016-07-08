package leval.gui.gameScreen

import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.scene.Node
import scalafx.scene.effect.{Light, Lighting}
import scalafx.scene.layout.{Region, StackPane}
import scalafx.util.Duration

/**
  * Created by lorilan on 6/29/16.
  */
class HighlightableRegion(decorated : Node) extends StackPane {

  protected val highlight = new Region {
    opacity = 0
    style = "-fx-border-width: 3; -fx-border-color: dodgerblue; -fx-background-color : rgba(12,56,100, 0.05);"
  }


  this.children = Seq(decorated, highlight)



  private val highlightTransition = new FadeTransition {
    node = highlight
    duration = Duration(200)
    fromValue = 0
    toValue = 1
  }

  val defaultEffect = effect

  def activateHighlight() = {
    highlightTransition.rate() = 1
    highlightTransition.play()

    effect = new Lighting {
      light = new Light.Distant {
        azimuth = -135
        elevation = 30
      }
    }
  }

  def deactivateHightLight() = {
    highlightTransition.rate = -1
    highlightTransition.play()
    effect = null.asInstanceOf[javafx.scene.effect.Lighting]
  }

}