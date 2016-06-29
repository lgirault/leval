package leval.gui.gameScreen

import leval._
import leval.core._
import leval.gui.{CardImageView, CardImg}

import scalafx.geometry.Point2D
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane

/**
  * Created by lorilan on 6/25/16.
  */

class CardDragAndDrop(c : Card, p : Pane) extends (MouseEvent => Unit) {

  var cardImageView : CardImageView = _

  var anchorPt: Point2D = null
  var previousLocation: Point2D = null

  def updateCoord(me : MouseEvent) : Unit = {
    cardImageView.x = me.sceneX - (CardImg.width / 2)
    cardImageView.y = me.sceneY - (CardImg.height / 5)
  }

  def apply(me : MouseEvent) : Unit = me.eventType match {
    case MouseEvent.MousePressed =>
      if(cardImageView == null) {
        cardImageView = CardImg(c)
      }
      anchorPt = new Point2D(me.sceneX, me.sceneY)
      p.children.add(cardImageView)
      updateCoord(me)

    case MouseEvent.MouseReleased =>
      ignore(p.children.remove(cardImageView))

    case MouseEvent.MouseDragged =>
      if (anchorPt != null) {
        previousLocation = anchorPt
        anchorPt = new Point2D(me.sceneX, me.sceneY)

        val tx = anchorPt.x - previousLocation.x
        val ty = anchorPt.y - previousLocation.y

        cardImageView.x.value = cardImageView.x.value + tx
        cardImageView.y.value = cardImageView.y.value + ty

      }
    case _ => ()
  }
}