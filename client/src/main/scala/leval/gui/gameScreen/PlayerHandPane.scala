package leval.gui.gameScreen

import leval.core.{Card, CardOrigin}

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{FlowPane, HBox}

/**
  * Created by lorilan on 7/5/16.
  */

class PlayerHandPane
( controller : GameScreenControl,
  height : Double) extends FlowPane {
  //style = "-fx-border-width: 1; -fx-border-color: black;"
  def hand = controller.game.stars(controller.playerGameIdx).hand

  private def images(hide : Seq[Card] = Seq()) : Seq[CardImageView]= {

    val cards = hand filterNot (hide contains _)

    val imgs =
      if(cards.isEmpty) Seq[CardImageView]()
      else cards.tail.foldLeft(Seq(CardImg.topHalf(cards.head, Some(height)))) {
        case (acc, c) =>
          val ci = CardImg.cutTopHalf(c, Some(height))
          ci.alignmentInParent = Pos.BottomCenter
          ci +: acc
      }

    imgs.foreach {
      img =>
        img.handleEvent(MouseEvent.Any) {
          new CardDragAndDrop(controller,
            controller.canDragAndDropOnInfluencePhase,
            CardOrigin.Hand(controller.playerGameIdx, img.card))
        }
    }
    imgs
  }

  val wrapper = new HBox(images(): _*)
  def update(filter : Seq[Card] = Seq()) : Unit = {
    wrapper.children.clear()
    wrapper.children = images(filter)

  }
  children = wrapper
  alignmentInParent = Pos.BottomCenter

}

class OpponnentHandPane
( controller : GameScreenControl,
  height : Double) extends FlowPane {
 // style = "-fx-border-width: 1; -fx-border-color: black;"
  def hand = controller.game.stars(controller.opponentId).hand

  private def images : Seq[CardImageView]=
      if(hand.isEmpty) Seq[CardImageView]()
      else hand.tail.foldLeft(Seq(CardImg.bottomHalf(hand.head, Some(height), front = false))) {
        case (acc, c) =>
          val ci = CardImg.cutBottomHalf(c, Some(height), front = false)
          ci.alignmentInParent = Pos.BottomCenter
          ci +: acc
      }

  val wrapper = new HBox(images: _*)
  def update() : Unit = {
    wrapper.children.clear()
    wrapper.children = images

  }
  children = wrapper
  alignmentInParent = Pos.TopCenter

}