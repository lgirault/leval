package leval.gui.gameScreen.being

import leval.core.{Card, Club, Diamond, Heart, Spade, Suit}
import leval.gui.gameScreen.CardDropTarget
import leval.gui.text.ValText

import scalafx.scene.Node

/**
  * Created by Loïc Girault on 02/09/16.
  */

//class EditableBeingTile
//(val backImg : Pane,
// val suit : Suit,
// val doOnDrop : CardOrigin => Unit)
//  extends CardDropTarget(backImg) {
//  def onDrop(origin: CardOrigin): Unit = doOnDrop(origin)
//}
//class EditableBeingPane
//(val cardWidth : Double,
// val cardHeight : Double)
//( implicit txt : ValText ) extends BeingGrid {
//
//}

abstract class EditableBeingTile
( val pos : Option[Suit],
  val cardWidth : Double,
  val cardHeight : Double,
  val txt : ValText)
  extends CardDropTarget {

  val backImg =
    cardRectangle(pos map txt.suitsText getOrElse txt.face,
      cardWidth, cardHeight)

  decorated = backImg

  def card : Option[Card]
}


abstract class EditableBeingPane[Tile <: EditableBeingTile]
  extends BeingGrid {
  val cardWidth : Double
  val okButton = okImage(cardWidth/3)
  okButton.visible = false
  val closeButton : Node = cancelImage(cardWidth/3)//closeCanvas(cardWidth)

  bottomLeftCenterConstraints(okButton)
  bottomRightCenterConstraints(closeButton)

  def makeTilePane(pos : Option[Suit]) : Tile

  val mind = makeTilePane(Some(Diamond))
  topConstraints(mind)
  val power = makeTilePane(Some(Club))
  bottomConstraints(power)
  val heart = makeTilePane(Some(Heart))
  leftConstraints(heart)
  val weapon = makeTilePane(Some(Spade))
  rightConstraints(weapon)

  val mapTiles : Map[Suit, EditableBeingTile] =
    Map(Diamond -> mind,
      Club -> power,
      Heart -> heart,
      Spade -> weapon)

  def resources : Map[Suit, Card] = mapTiles.foldLeft(Map[Suit, Card]())  {
    case (m, (s, t)) =>
      t.card match {
        case None => m
        case Some(c) => m + (s -> c)
      }
  }

}