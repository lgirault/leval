package leval.gui.gameScreen

import javafx.geometry.Bounds

import leval.ignore
import leval.core._

import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.image.ImageView
import scalafx.scene.input.MouseEvent

/**
  * Created by lorilan on 6/25/16.
  */

object CardDragAndDrop {

  implicit class NodeOps(val n : Node) extends AnyVal {
    def boundsInScene : Bounds =
      n.localToScene(n.boundsInLocal.value)
  }
}

import leval.gui.gameScreen.CardDragAndDrop.NodeOps
class CardDragAndDrop
( control: GameScreenControl,
  canDragAndDrop : () => Boolean,
  origin : Origin)
( val cardImageView : ImageView = CardImg(origin.card))
  extends (MouseEvent => Unit) {

  import control.pane

  var anchorPt: Point2D = new Point2D(0,0)
  var previousLocation: Point2D = anchorPt

  cardImageView.managed = false

  def updateCoord(me : MouseEvent) : Unit = {
    cardImageView.x = me.sceneX - (CardImg.width / 2)
    cardImageView.y = me.sceneY - (CardImg.height / 5)
  }



  def apply(me : MouseEvent) : Unit = me.eventType match {
    case MouseEvent.MousePressed if canDragAndDrop() =>

      pane.doHightlightTargets(origin)
      anchorPt = new Point2D(me.sceneX, me.sceneY)
      cardImageView.visible = true
      pane.children.add(cardImageView)
      updateCoord(me)

    case MouseEvent.MouseReleased =>

      val cardBounds = cardImageView.boundsInScene
      pane.highlightedTargets.find {
        tgt =>
          tgt.boundsInScene intersects cardBounds
      } foreach {
        cardImageView.visible = false
        _.onDrop(origin)
      }
      pane.unHightlightTargets()
      leval.ignore(pane.children.remove(cardImageView.delegate))

    case MouseEvent.MouseDragged =>

        previousLocation = anchorPt
        anchorPt = new Point2D(me.sceneX, me.sceneY)

        val tx = anchorPt.x - previousLocation.x
        val ty = anchorPt.y - previousLocation.y

        cardImageView.x.value = cardImageView.x.value + tx
        cardImageView.y.value = cardImageView.y.value + ty


    case _ => ()
  }
}
