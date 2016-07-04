package leval.gui.gameScreen

import javafx.geometry.Bounds

import leval._
import leval.core._
import leval.gui.{CardImageView, CardImg}

import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane

/**
  * Created by lorilan on 6/25/16.
  */

sealed abstract class Origin
case object Hand extends Origin
case object BeingPanel extends Origin
case object CreateBeingPanel extends Origin

object CardDragAndDrop {

  implicit class NodeOps(val n : Node) extends AnyVal {
    def boundsInScene : Bounds =
      n.localToScene(n.boundsInLocal.value)
  }
}
import CardDragAndDrop.NodeOps
class CardDragAndDrop
(scene: TwoPlayerGameScene,
 numStar : Int,
 c : Card, origin : Origin) extends (MouseEvent => Unit) {

  def pane = scene.bpRoot
  val oGame = scene.oGame
  var cardImageView : CardImageView = _

  var anchorPt: Point2D = null
  var previousLocation: Point2D = null


  def updateCoord(me : MouseEvent) : Unit = {
    cardImageView.x = me.sceneX - (CardImg.width / 2)
    cardImageView.y = me.sceneY - (CardImg.height / 5)
  }

  def canDragAndDrop : Boolean =
    oGame.currentPlayer == numStar &&
      oGame.roundState == InfluencePhase


  def apply(me : MouseEvent) : Unit = me.eventType match {
    case MouseEvent.MousePressed if canDragAndDrop =>
      if(cardImageView == null) {
        cardImageView = CardImg(c)
      }
      scene.doHightlightTargets(origin, c)
      anchorPt = new Point2D(me.sceneX, me.sceneY)
      pane.children.add(cardImageView)
      updateCoord(me)

    case MouseEvent.MouseReleased =>

      val cardBounds = cardImageView.boundsInScene

      //println("[CARD] " + cardImageView.boundsInScene)
      scene.highlightedTargets.find {
        tgt =>
        //  println("[TARGET] " + tgt.boundsInScene)
          tgt.boundsInScene intersects cardBounds
      } foreach { _.onDrop(c, origin)}
      //println()

      scene.unHightlightTargets()
      ignore(pane.children.remove(cardImageView))


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