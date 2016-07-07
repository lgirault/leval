package leval.gui.gameScreen

import leval.core.{Being, Card, Club, Diamond, Heart, Spade, Suit}
import leval.gui.CardImg

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Node
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

/**
  * Created by Loïc Girault on 05/07/16.
  */
sealed abstract class Orientation{
  def top(b : Being) : Option[Card]
  def left(b : Being) : Option[Card]
  def right(b : Being) : Option[Card]
  def bottom(b : Being) : Option[Card]
}
case object Player extends Orientation{
  def top(b : Being) : Option[Card] = b.mind
  def left(b : Being) : Option[Card] = b.heart
  def right(b : Being) : Option[Card] = b.weapon
  def bottom(b : Being) : Option[Card] = b.power
}
case object Opponent extends Orientation {
  def top(b : Being) : Option[Card] = b.power
  def left(b : Being) : Option[Card] = b.weapon
  def right(b : Being) : Option[Card] = b.heart
  def bottom(b : Being) : Option[Card] = b.mind
}

class BeingResourcePane
(bp : BeingPane,
 val card : Card,
 val position : Suit, // needed for lovers and Jokers
 cardDragAndDrop: CardDragAndDrop)
(val backImg : Node = CardImg.back(bp.cardHeight))
  extends CardDropTarget(backImg) {

  val frontImg : Node = CardImg(card, bp.cardHeight)

  private [this] var subscription  : Option[Subscription] = None
  def unsetCardDragAndDrop() : Unit =
    subscription foreach (_.cancel())

  def setCardDragAndDrap() : Unit = {
    subscription = Some(handleEvent(MouseEvent.Any) {
      cardDragAndDrop
    })
  }

  setCardDragAndDrap()

  def reveal() : Unit = {
    children = Seq(frontImg, highlight)
  }
  def hide() : Unit = {
    children = Seq(backImg, highlight)
  }

  def onDrop(c : Card, origin : Origin) : Unit =
      bp.control.playOnBeing(c, origin, bp.being, position)

}

class BeingPane
( val control: GameScreenControl,
  var being : Being,
  val cardHeight : Double,
  cardWidth : Double,
  val orientation: Orientation) extends GridPane {


  val rowCts = new RowConstraints(
    minHeight = cardHeight,
    prefHeight = cardHeight,
    maxHeight = cardHeight)

  val colCts = new ColumnConstraints(
    minWidth = cardWidth,
    prefWidth = cardWidth,
    maxWidth = cardWidth
  )

  for (i <- 0 until 3) {
    rowConstraints.add(rowCts)
    columnConstraints.add(colCts)
  }

  def topCard  = orientation.top(being)
  def leftCard = orientation.left(being)
  def rightCard = orientation.right(being)
  def bottomCard  = orientation.bottom(being)

  private [this] var resourcePanes0 = Seq[BeingResourcePane]()
  def resourcePanes = resourcePanes0

  def resourcePane(s : Suit) : Option[BeingResourcePane] = {
    resourcePanes0 find (_.position == s)
  }

  def reveal(s : Suit) : Unit =
    resourcePane(s) foreach (_.reveal())

  def placeResourcePane( c : Card, pos : Suit, place : Node => Unit) : Node ={
    val cardDragAndDrop =
      new CardDragAndDrop(control, control.canDragAndDropOnActPhase(being.face), c,
        Origin.BeingPane(being, pos))(CardImg(c, front = false))

    val bpr = new BeingResourcePane(this, c, pos, cardDragAndDrop)()
    resourcePanes0 +:= bpr
    place(bpr)
    bpr
  }

  if(leftCard.nonEmpty) {
    placeResourcePane(leftCard.get, Heart,
      GridPane.setConstraints(_, 0, 1))
  }

  if(rightCard.nonEmpty){
    placeResourcePane(rightCard.get, Spade,
      GridPane.setConstraints(_, 2, 1))

  }

  if(topCard.nonEmpty)
    placeResourcePane(topCard.get, Diamond,
      GridPane.setConstraints(_, 1, 0))

  if(bottomCard.nonEmpty)
    placeResourcePane(bottomCard.get, Club,
      GridPane.setConstraints(_, 1, 2))

  val faceImage = CardImg(being.face, cardHeight)
  GridPane.setConstraints(faceImage, 1, 1)
  children = faceImage +: resourcePanes
}
