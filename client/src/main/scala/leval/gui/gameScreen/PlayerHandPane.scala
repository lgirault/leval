package leval.gui.gameScreen

import leval.core.Card
import leval.gui.{CardImageView, CardImg}

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{FlowPane, HBox}

/**
  * Created by lorilan on 7/5/16.
  */
class PlayerHandPane
( controller : GameScreenControl) extends FlowPane {

  def hand = controller.oGame.stars(controller.playerGameId).hand



  private def images(hide : Seq[Card] = Seq()) : Seq[CardImageView]= {

    val cards = hand filterNot (hide contains _)

    val imgs =
      if(cards.isEmpty) Seq[CardImageView]()
      else cards.tail.foldLeft(Seq(CardImg.topHalf(cards.head))) {
        case (acc, c) =>
          val ci = CardImg.cutTopHalf(c)
          ci.alignmentInParent = Pos.BottomCenter
          ci +: acc
      }

    imgs.foreach {
      img =>
        img.handleEvent(MouseEvent.Any) {
          new CardDragAndDrop(controller,
            controller.canDragAndDropOnInfluencePhase,
            img.card, Origin.Hand)()
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