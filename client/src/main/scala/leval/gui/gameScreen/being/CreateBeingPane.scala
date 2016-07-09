package leval.gui.gameScreen.being

import leval.core._
import leval.gui.gameScreen._
import leval.gui.text.ValText
import leval.gui.{CardImageView, CardImg}

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color

/**
  * Created by lorilan on 7/4/16.
  */

class CreateBeingTile
(val pane : CreateBeingPane,
 val tile : Pane,
 val hand : PlayerHandPane,
 doCardDragAndDrop: Origin => CardDragAndDrop )
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
  def card_=(c : Card) = {
    val ci = CardImg(c, Some(tile.prefHeight.value))
    cardImg = Some((ci, doCardDragAndDrop(Origin.CreateBeingPane(c))))
  }

  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      cardImg.foreach {
        case (civ, cdad) => cdad(me)
      }
  }

  def onDrop(origin : Origin) : Unit = {
    card = origin.card
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


  val okButton = okCanvas(cardWidth)
  okButton.visible = false

  okButton.onMouseClicked = {
    me : MouseEvent =>
      being foreach controller.placeBeing
  }

  def editMode(origin: Origin) : Unit = {
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
      def onDrop(origin: Origin) =
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

  def being : Option[Being] = {
    val m0 = Map[Suit, Card]()
    val m1 = mind.card map (c => m0 + (Diamond -> c)) getOrElse m0
    val m2 = power.card map (c => m1 + (Club -> c)) getOrElse m1
    val m3 = heart.card map (c => m2 + (Heart -> c)) getOrElse m2
    val m =  weapon.card map (c => m3 + (Spade -> c)) getOrElse m3

    face.card map {
      case fc : Card => new Being(fc, m,
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

  def targets(c : Card ): Seq[CardDropTarget] =
    defaultPos(c) +: (
      face.card map {
        fc =>
          (fc, c) match {
            case (C(Queen, fsuit), C(King, suit)) if fsuit == suit =>
              Seq(heart)
            case (C(King, fsuit), C(Queen, suit)) if fsuit == suit =>
              Seq(heart)
            case _ => Seq()
          }
      } getOrElse Seq())

  def legalFormation : Boolean =
    face.card.nonEmpty && (
    (heart.card, weapon.card, mind.card, power.card) match {
      case Formation(_) => true
      case _ => false
    })
}
