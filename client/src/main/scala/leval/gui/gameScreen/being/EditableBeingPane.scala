package leval.gui.gameScreen.being

import leval.core.{Card, Club, Diamond, Heart, Spade, Suit}
import leval.gui.gameScreen.{CardDropTarget, CardImg, PlayerHandPane}
import leval.gui.text.ValText

import scalafx.scene.Node

/**
  * Created by Loïc Girault on 02/09/16.
  */

abstract class EditableBeingTile
  extends CardDropTarget {

  val pane : EditableBeingPane[_]
  val backImg : Node

  def setCardImg(sc : Option[(Card, Boolean)]) : Unit = sc match {
    case None => decorated = backImg
    case Some((c, front)) =>
      decorated = CardImg(c, Some(pane.cardHeight), front)
  }

  def card : Option[Card]
  def card_=(sc : Option[(Card, Boolean)]) : Unit =
    setCardImg(sc)

}

abstract class EditableBeingResourceTile
  extends EditableBeingTile {

  val pos : Suit

  val backImg : Node =
    cardRectangle(pane.txt suitsText pos,
      pane.cardWidth,
      pane.cardHeight)

  decorated = backImg

  def card : Option[Card] = pane.resources get pos
  override def card_=(sc : Option[(Card, Boolean)]) = {
    super.card_=(sc)
    sc match {
      case None => pane.resources -= pos
      case Some((c, _)) => pane.resources += (pos -> c)
    }
  }

}

abstract class EditableBeingPane[Tile <: EditableBeingTile]
  extends BeingGrid {

  val cardWidth : Double
  val cardHeight : Double
  val txt : ValText

  val okButton = okImage(cardWidth/3)
  okButton.visible = false
  val closeButton : Node = cancelImage(cardWidth/3)//closeCanvas(cardWidth)

  bottomLeftCenterConstraints(okButton)
  bottomRightCenterConstraints(closeButton)


  def makeResourceTilePane(pos : Suit) : Tile

  val mind = makeResourceTilePane(Diamond)
  topConstraints(mind)
  val power = makeResourceTilePane(Club)
  bottomConstraints(power)
  val heart = makeResourceTilePane(Heart)
  leftConstraints(heart)
  val weapon = makeResourceTilePane(Spade)
  rightConstraints(weapon)

  def mapTiles : Map[Suit, Tile] =
    Map(Diamond -> mind,
      Club -> power,
      Heart -> heart,
      Spade -> weapon)

  var resources = Map[Suit, Card]()

  def legalFormation : Boolean

  def faceCard : Option[Card]
  val tiles : List[Tile] = List(mind, power, heart, weapon)
  def cards : List[Card] = (faceCard :: (tiles map (_.card))).flatten
  val hand : PlayerHandPane

  def checkIfLegalAndUpdate() : Unit = {
    if(legalFormation)
      okButton.visible = true
    else
      okButton.visible = false
    hand update cards
  }
}