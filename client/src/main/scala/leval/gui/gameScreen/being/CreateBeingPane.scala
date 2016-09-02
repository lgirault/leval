package leval.gui.gameScreen.being

import leval.core._
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._

/**
  * Created by lorilan on 7/4/16.
  */

class CreateBeingTile
(val pane : CreateBeingPane,
 val hand : PlayerHandPane,
 override val pos : Option[Suit],
 doCardDragAndDrop: CardOrigin => CardDragAndDrop )
  extends EditableBeingTile(
    pos,
    pane.cardWidth,
    pane.cardHeight,
    pane.txt ) {

  decorated = backImg

  var cardImg0 : Option[(CardImageView, CardDragAndDrop)] = None
  def cardImg : Option[(CardImageView, CardDragAndDrop)] = cardImg0
  def cardImg_=(v : Option[(CardImageView, CardDragAndDrop)]) = {
    if(cardImg0.nonEmpty)
      backImg.children.remove(cardImg0.get._1)

    if(v.nonEmpty)
      backImg.children.add(v.get._1)

    cardImg0 = v
  }

  def card = cardImg0 map (_._1.card)
  def card_=(sc : Option[Card]) =
    cardImg = sc map {
      c =>
        val ci = CardImg(c, Some(backImg.prefHeight.value))
        (ci, doCardDragAndDrop(CardOrigin.Hand(pane.playerGameIdx , c)))
    }


  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      cardImg.foreach {
        case (civ, cdad) => cdad(me)
      }
  }

  def onDrop(origin : CardOrigin) : Unit = {
    (pane.tiles find( p => p != this && (p.card contains origin.card) ), card) match {
      case (Some(otherPane), Some(thisCard)) => //card comes from othePane

        val switch = otherPane.pos match {
          case None => //other pane is face
            pane.rules.checkLegalLover(origin.card, thisCard)
          case Some(otherPos) =>
            pane.rules.validResource(pane.face.card,
              pane.resources, thisCard, otherPos)
        }

        println(s"switch = $switch")
        if(switch)
          otherPane.card = Some(thisCard)
        else
          otherPane.card = None


      case (Some(otherPane), None) => //joker can be moved from one place to the other
        otherPane.card = None
      case _ => ()
    }

    card = Some(origin.card)

    if(pane.legalFormation)
      pane.okButton.visible = true
    else
      pane.okButton.visible = false

    hand.update(pane.cards)
  }
}



class CreateBeingPane
( controller : GameScreenControl,
  hand : PlayerHandPane,
  val cardWidth : Double,
  val cardHeight : Double)
( implicit val txt : ValText )
  extends EditableBeingPane[CreateBeingTile] {

  private [this] var open0 : Boolean = false

  def isOpen = open0

  def playerGameIdx = controller.playerGameIdx

  okButton.onMouseClicked = {
    me : MouseEvent =>
      being foreach controller.placeBeing
  }

  def editMode(origin: CardOrigin) : Unit = {
    children = Seq(face, mind, power, heart, weapon,
      okButton, closeButton/*buttonWrapper*/)
    defaultPos(origin.card).onDrop(origin)
    open0 = true
  }

  def menuMode() : Unit = {
    children = createBeingLabel
    open0 = false
    tiles.foreach (_.cardImg = None)
    hand.update()
  }

  val createBeingLabel = new CardDropTarget {
      decorated = cardRectangle(txt.create_being, cardWidth, cardHeight)
      def onDrop(origin: CardOrigin) =
        editMode(origin)
    }


  def makeTilePane(pos : Option[Suit]) = {
    val t = pos map txt.suitsText getOrElse txt.face
    new CreateBeingTile(this,
      hand, pos,
      new CardDragAndDrop(controller,
        controller.canDragAndDropOnInfluencePhase, _))
  }


  GridPane.setConstraints(createBeingLabel, 1, 1)
  children = createBeingLabel
  val face = makeTilePane(None)
  centerConstraints(face)




  val tiles : Seq[CreateBeingTile] = Seq(face, mind, power, heart, weapon)

  def cards : Seq[Card] = tiles flatMap (_.card)

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

    val allowedTiles : Seq[CardDropTarget] = defaultPos(c) :: (mapTiles filter {
      case (pos, tile) => rules.validResource(face.card, resources, c, pos)
    } values).toList

    (face.card , c) match {
      case (Some(Card(lover@(King | Queen), fsuit)), Card(r : Face, s))
        if fsuit == s && rules.otherLover(lover) == r =>
        heart +: allowedTiles
      case _ => allowedTiles
    }
  }

  def legalFormation : Boolean =
    being match {
      case None => false
      case Some(b @ Formation(f)) =>
      rules.validBeing(b) && (!b.lover ||
        rules.legalLoverFormationAtCreation(f)) && {

        val playerBeings = controller.game beingsOwnBy controller.playerGameIdx

        val hasSameFormation = playerBeings exists {
          case Formation(`f`) => true
          case _ => false
        }
        !hasSameFormation
      }
      case _ => false
  }
}
