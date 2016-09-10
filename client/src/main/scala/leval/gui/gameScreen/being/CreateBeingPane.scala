package leval.gui.gameScreen.being

import leval.core._
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.input.MouseEvent

/**
  * Created by lorilan on 7/4/16.
  */


trait CreateBeingTile {
  self : CardDropTarget =>

  def pane : CreateBeingPane
  def card : Option[Card]
  def card_=(c : Option[(Card, Boolean)]) : Unit

  var dand : Option[CardDragAndDrop] = None


  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      dand foreach (_(me))
  }

  //precond card.nonEmpty
  def switchPredicate(resourceTile : CreateBeingResourceTile) : Boolean

  def switchIfNeeded(ocard : Card) = {
    (pane.tiles find( p => p != this && (p.card contains ocard)), card) match {
      case (Some(otherPane), Some(thisCard)) => //card comes from othePane
        if(switchPredicate(otherPane))
          otherPane.card = Some((thisCard, true))
        else otherPane.card = None
      case (Some(otherPane), None) =>
        otherPane.card = None
      case _ => ()
    }
  }

  def onDrop(origin : CardOrigin) : Unit = {
    switchIfNeeded(origin.card)
    card = Some((origin.card, true))
    pane.checkIfLegalAndUpdate()
  }

}

class CreateBeingFaceTile
(val pane : CreateBeingPane)
  extends EditableBeingTile
    with CreateBeingTile {

  val backImg =
    cardRectangle(pane.txt.face,
      pane.cardWidth,
      pane.cardHeight)

  decorated = backImg

  private [this] var card0 : Option[Card] = None


  def card : Option[Card] = card0
  override def card_=(sc : Option[(Card, Boolean)]) : Unit = {
    super.card_=(sc)
    card0 = sc map (_._1)
    dand = card0 map pane.doCardDragAndDrop
  }

  def switchPredicate(resourceTile : CreateBeingResourceTile) : Boolean =
    resourceTile.pos match {
      case Heart =>
        pane.rules.checkLegalLover(resourceTile.card.get, card.get)
      case otherPos =>
        pane.rules.validResource(pane.face.card,
          pane.resources, card.get, otherPos)
    }

  def removeHeartIfNeeded(newCard : Card) : Unit =
    pane.resources get Heart map {
      case Joker(_) | Card(Numeric(_), _) => false
      case Card(Jack, s) =>
        ! newCard.isInstanceOf[C] ||
          s != newCard.asInstanceOf[C].suit
      case Card(r @ (King|Queen), s) =>
        ! newCard.isInstanceOf[C] || {
          val nc = newCard.asInstanceOf[C]
          s != nc.suit || pane.rules.otherLover(r) != nc.rank
        }
    } foreach  {
      needRemove =>
        if(needRemove)
          pane.heart.card = None
    }





  override def onDrop(origin : CardOrigin) : Unit = {
    removeHeartIfNeeded(origin.card)
    super.onDrop(origin)
  }


}


class CreateBeingResourceTile
(val pane : CreateBeingPane,
 val pos : Suit)
  extends EditableBeingResourceTile
    with CreateBeingTile {

  def switchPredicate(resourceTile : CreateBeingResourceTile) : Boolean = {
    pane.rules.validResource(pane.face.card,
      pane.resources, card.get, resourceTile.pos)
  }

  override def switchIfNeeded(ocard : Card) : Unit =
    if(pane.face.card contains ocard) {
      card match {
        case Some(thisCard)
          if pane.rules.checkLegalLover(ocard, thisCard) =>
          pane.face.card = Some((thisCard, true))
        case _ =>
          pane.face.card = None
      }
    }
    else super.switchIfNeeded(ocard)

  override def card_=(sc : Option[(Card, Boolean)]) : Unit = {
    super.card_=(sc)
    dand = sc map (_._1) map pane.doCardDragAndDrop
  }
}


class CreateBeingPane
( val controller : GameScreenControl,
  val hand : PlayerHandPane,
  val cardWidth : Double,
  val cardHeight : Double)
( implicit val txt : ValText )
  extends EditableBeingPane[CreateBeingResourceTile] {

  private [this] var open0 : Boolean = false

  def isOpen = open0

  def playerGameIdx = controller.playerGameIdx

  okButton.onMouseClicked = {
    me : MouseEvent =>
      being foreach controller.placeBeing
  }

  def editMode(origin: CardOrigin) : Unit = {
    children = Seq(face, mind, power, heart, weapon,
      okButton, closeButton)
    defaultPos(origin.card).onDrop(origin)
    open0 = true
  }

  def menuMode() : Unit = {
    children = createBeingLabel
    open0 = false
    tiles.foreach (_.card = None)
    face.card = None
    hand.update()
  }

  val createBeingLabel = new CardDropTarget {
    decorated = cardRectangle(txt.create_being, cardWidth, cardHeight)
    def onDrop(origin: CardOrigin) =
      editMode(origin)
  }

  def doCardDragAndDrop(c : Card) : CardDragAndDrop =
    new HandDragAndDrop(controller, c)

  def makeResourceTilePane(pos : Suit) =
    new CreateBeingResourceTile(this,  pos)

  BeingGrid.centerConstraints(createBeingLabel)
  children = createBeingLabel

  val face = new CreateBeingFaceTile(this)
  BeingGrid.centerConstraints(face)

  def faceCard = face.card

  def being : Option[Being] = {
    face.card map {
      case fc : Card => new Being(playerGameIdx,
        fc, resources, heart.card exists {
          case Card(King | Queen, _) => true
          case _ => false
        }
      )
      case _ => leval.error()
    }
  }

  closeButton.onMouseClicked = {
    me : MouseEvent =>
      menuMode()
  }

  def defaultPos(c : Card) : CardDropTarget =
    c match {
      case Card(_ : Face, _) | Joker(_) => face
      case Card(_, Heart) => heart
      case Card(_, Club) => power
      case Card(_, Diamond) => mind
      case Card(_, Spade) => weapon
    }

  val rules = controller.game.rules

  def targets(c : Card ): Seq[CardDropTarget] = {

    val allowedTiles : Seq[CardDropTarget] =
      c match {
        case Joker(_)
          if resources.values exists (c2 => c2.isInstanceOf[J] && c2 != c) => Seq()
        case _ => mapTiles.foldLeft(List(defaultPos(c))) {
          case (l, (pos, tile)) =>
            if(rules.validResource(face.card, resources, c, pos))
              tile :: l
            else l
        }
      }

    (face.card , c) match {
      case (Some(Card(lover@(King | Queen), fsuit)), Card(r : Face, s))
        if fsuit == s && rules.otherLover(lover) == r =>
        heart +: allowedTiles
      case _ => allowedTiles
    }
  }

  def legalFormation : Boolean = {
    being exists (rules.isValidBeingAtCreation(controller.game.game, _,
      controller.playerGameIdx))
  }

}
