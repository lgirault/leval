package leval.gui.gameScreen.being

import leval.core._
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._

/**
  * Created by lorilan on 7/4/16.
  */

class CreateBeingTile
(val pane : CreateBeingPane,
 val tile : Pane,
 val hand : PlayerHandPane,
 doCardDragAndDrop: CardOrigin => CardDragAndDrop )
  extends CardDropTarget(tile) {

  var cardImg0 : Option[(CardImageView, CardDragAndDrop)] = None
  def cardImg : Option[(CardImageView, CardDragAndDrop)] = cardImg0
  def cardImg_=(v : Option[(CardImageView, CardDragAndDrop)]) = {
    if(cardImg0.nonEmpty)
      tile.children.remove(cardImg0.get._1)

    if(v.nonEmpty)
      tile.children.add(v.get._1)

    cardImg0 = v
  }

  def card = cardImg0 map (_._1.card)
  def card_=(sc : Option[Card]) =
    cardImg = sc map {
      c =>
        val ci = CardImg(c, Some(tile.prefHeight.value))
        (ci, doCardDragAndDrop(CardOrigin.Hand(pane.playerGameIdx , c)))
    }


  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      cardImg.foreach {
        case (civ, cdad) => cdad(me)
      }
  }

  def pos : Option[Suit] = pane.mapTiles.find( _._2 == this) map (_._1)

  def onDrop(origin : CardOrigin) : Unit = {
    (pane.tiles find( p => p != this && (p.card contains origin.card) ), card) match {
      case (Some(otherPane), Some(thisCard)) => //card comes from othePane

        val switch = otherPane.pos match {
          case None => //other pane is face
            pane.rules.checkLegalLover(origin.card, thisCard)
          case Some(otherPos) =>
            pane.rules.validResource(pane.face.card getOrElse Joker.Black, thisCard, otherPos)
        }

        if(switch)
          otherPane.card = Some(thisCard)
        else
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
( implicit txt : ValText ) extends BeingGrid {

  private [this] var open0 : Boolean = false

  def isOpen = open0

  def playerGameIdx = controller.playerGameIdx

  val okButton = okCanvas(cardWidth)
  okButton.visible = false

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

  val createBeingLabel =
    new CardDropTarget(cardRectangle(txt.create_being, cardWidth, cardHeight)) {
      def onDrop(origin: CardOrigin) =
        editMode(origin)
    }


  def makeTilePane(txt : String) =
    new CreateBeingTile(this,
      cardRectangle(txt, cardWidth, cardHeight),
      hand,
      new CardDragAndDrop(controller,
        controller.canDragAndDropOnInfluencePhase, _)())

  GridPane.setConstraints(createBeingLabel, 1, 1)
  children = createBeingLabel
  val face = makeTilePane(txt.face)
  centerConstraints(face)
  val mind = makeTilePane(txt.mind)
  topConstraints(mind)
  val power = makeTilePane(txt.power)
  bottomConstraints(power)
  val heart = makeTilePane(txt.heart)
  leftConstraints(heart)
  val weapon = makeTilePane(txt.weapon)
  rightConstraints(weapon)


  val tiles : Seq[CreateBeingTile] = Seq(face, mind, power, heart, weapon)

  def cards : Seq[Card] = tiles flatMap (_.card)

  val mapTiles : Map[Suit, CreateBeingTile] =
    Map(Diamond -> mind,
      Club -> power,
      Heart -> heart,
      Spade -> weapon)

  def being : Option[Being] = {

    val m0 = Map[Suit, Card]()
    val m1 = mind.card map (c => m0 + (Diamond -> c)) getOrElse m0
    val m2 = power.card map (c => m1 + (Club -> c)) getOrElse m1
    val m3 = heart.card map (c => m2 + (Heart -> c)) getOrElse m2
    val m =  weapon.card map (c => m3 + (Spade -> c)) getOrElse m3

    face.card map {
      case fc : Card => new Being(playerGameIdx,
        fc, m,
        heart.card exists {
          case C(King | Queen, _) => true
          case _ => false
        }
      )
      case _ => leval.error()
    }
  }


  val closeButton : Node = closeCanvas(cardWidth)
  closeButton.onMouseClicked = {
    me : MouseEvent =>
      menuMode()
  }

  GridPane.setConstraints(okButton, 0, 2)
  //okButton.alignmentInParent = Pos.BottomLeft

  GridPane.setConstraints(closeButton, 2, 2)
  //  closeButton.alignmentInParent = Pos.BottomRight

  //  val buttonWrapper = new VBox {
  //    val vspacer = new Region()
  //    VBox.setVgrow(vspacer, Priority.Always)
  //
  //    val hspacer = new Region()
  //    HBox.setHgrow(hspacer, Priority.Always)
  //
  //
  //    children = Seq(vspacer,
  //      new HBox(okButton, hspacer, closeButton))
  //  }
  //  GridPane.setConstraints(buttonWrapper, 2, 2)
  def defaultPos(c : Card) : CardDropTarget =
    c match {
      case C(_ : Face, _) | Joker(_) => face
      case C(_, Heart) => heart
      case C(_, Club) => power
      case C(_, Diamond) => mind
      case C(_, Spade) => weapon
    }

  val rules = controller.game.rules

  def targets(c : Card ): Seq[CardDropTarget] = {

    val allowedTiles : Seq[CardDropTarget] = defaultPos(c) :: (mapTiles filter {
      case (pos, tile) => rules.validResource(face.card getOrElse Joker.Black, c, pos)
    } values).toList

    (face.card , c) match {
      case (Some(C(lover@(King | Queen), fsuit)), C(r : Face, s))
        if fsuit == s && rules.otherLover(lover) == r =>
        heart +: allowedTiles
      case _ => allowedTiles
    }
  }

  def legalFormation : Boolean =
    being match {
      case None => false
      case Some(b @ Formation(f))=>
      rules.validBeing(b) && !(b.lover ||
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
