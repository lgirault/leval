package leval.gui.gameScreen

import javafx.geometry.Bounds

import leval.core._

import scalafx.geometry.Point2D
import scalafx.scene.Node
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
abstract class CardDragAndDrop extends (MouseEvent => Unit) {

  val control: GameScreenControl
  def canDragAndDrop() : Boolean
  val showFront : Boolean

  def origin : CardOrigin

  import control.pane

  lazy val cardImageView : CardImageView = {
    val height = pane.cardHeight
    val img = CardImg(origin.card,
      sfitHeight = Some(height),
      showFront)
    img.managed = false
    img
  }

  var anchorPt: Point2D = new Point2D(0,0)
  var previousLocation: Point2D = anchorPt

  def updateCoord(me : MouseEvent) : Unit = {

    val cardWidth = cardImageView.getBoundsInLocal.getWidth
    cardImageView.x = me.sceneX - control.leftPadding - (cardWidth / 2)
    cardImageView.y = me.sceneY - control.topPadding - (cardImageView.fitHeight() / 5)
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
      val stgt = pane.highlightedTargets.find {
        tgt =>
          tgt.boundsInScene intersects cardBounds
      }
      pane.unHightlightTargets()
      leval.ignore(pane.children.remove(cardImageView.delegate))
      stgt foreach {
        cardImageView.visible = false
        _.onDrop(origin)
      }

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

class HandDragAndDrop
( val control: GameScreenControl,
  card : Card
) extends  CardDragAndDrop {

  val origin : CardOrigin =
    CardOrigin.Hand(control.playerGameIdx, card)

  def canDragAndDrop() : Boolean =
    control.canDragAndDropOnInfluencePhase()

  val showFront : Boolean = true
}

class BeingDragAndDrop
(val control: GameScreenControl,
 face : Card,
 suit : Suit
) extends  CardDragAndDrop {

  def origin : CardOrigin =
    CardOrigin.Being(control.game.beings(face), suit)

  def canDragAndDrop() : Boolean =
    control canDragAndDropOnActPhase face


  val showFront : Boolean = false
}