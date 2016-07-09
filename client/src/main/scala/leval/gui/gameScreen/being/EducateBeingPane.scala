package leval.gui.gameScreen.being

import leval.core.{Being, C, Card, Club, Diamond, EducationType, Face, Formation, Heart, Joker, King, Queen, Rise, Spade, Switch}
import leval.gui.{CardImageView, CardImg}
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{GridPane, Pane}


/**
  * Created by lorilan on 7/9/16.
  */


class EducateBeingTile
(val pane : EducateBeingPane,
 val backImg : Pane,
 val hand : PlayerHandPane)
  extends CardDropTarget(backImg) {

  val switchImg : Node = switchImage(pane.cardWidth)
  switchImg.visible = false

  var cardImg0 : Option[ImageView] = None
  def cardImg : Option[ImageView]= cardImg0
  def cardImg_=(v : Option[ImageView]) = {
    v match {
      case None =>
        children = Seq(backImg, highlight)
      case Some(iv) =>
        children = Seq(switchImg, iv, highlight)

    }
    cardImg0 = v
  }

  private [this] var card0 : Option[Card] = None
  def card = card0
  def card_=(c : Card) ={
    card0 = Some(c)
    cardImg = Some(CardImg(c, Some(backImg.prefHeight.value)))
  }

  def doOnDrop(c : C) : Unit = {
    card = c
    if(pane.legalFormation)
      pane.okButton.visible = true
    else
      pane.okButton.visible = false

    hand.update(pane.cards)
  }

  def onDrop(origin : Origin) : Unit =
  origin.card match {
    case c : C =>

    pane.sEducation match {
      case None =>
        if(cardImg0.nonEmpty)
          pane.sEducation = Some(Switch(c))
        else
          pane.sEducation = Some(Rise(Seq(c)))
        doOnDrop(c)
      case Some(Switch(c2)) if c2.suit == c.suit =>
        pane.sEducation = Some(Switch(c2))
        doOnDrop(c2)
      case Some(Switch(c2))  =>
        new Alert(AlertType.Information) {
          delegate.initOwner(pane.scene().getWindow)
          title = "Education"
          headerText = s"Only one switch at a time"
        }.showAndWait()

      case Some(Rise(s)) =>
        if(cardImg0.nonEmpty)
          new Alert(AlertType.Information) {
            delegate.initOwner(pane.scene().getWindow)
            title = "Education"
            headerText = s"Switch or rise not both at the same time"
          }.showAndWait()
        else {
          val newS = s map (c2 =>
            if (c2.suit == c.suit) c
            else c2)
          pane.sEducation = Some(Rise(newS))
        }

    }
    case _ => ()
  }
}

class EducateBeingPane
(controller : GameScreenControl,
 hand : PlayerHandPane,
 val cardWidth : Double,
 val cardHeight : Double )
( implicit txt : ValText ) extends BeingGrid {

  style = "-fx-border-width: 3; " +
    "-fx-border-color: black; " +
    "-fx-background-color : green;"

  alignmentInParent = Pos.Center

  private [this] var being0 : Being = _

  def makeTilePane(txt : String) =
    new EducateBeingTile(this,
      cardRectangle(txt, cardWidth, cardHeight),
      hand)


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


  def being_=(b : Being) : Unit = {
    being0 = b
    face.card = b.face
    b.mind foreach mind.card_=
    b.heart foreach heart.card_=
    b.power foreach power.card_=
    b.weapon foreach weapon.card_=

    this.visible = true

  }

  def being = being0

  var sEducation : Option[EducationType] = None

  val okButton = okCanvas(cardWidth)
  okButton.visible = false
  okButton.onMouseClicked = {
    me : MouseEvent =>
      sEducation foreach (e =>
        controller.educate(being.face, e.cards))

  }

  val closeButton : Node = closeCanvas(cardWidth)
  closeButton.onMouseClicked = {
    me : MouseEvent =>
      this.visible = false
  }

  GridPane.setConstraints(okButton, 0, 2)
  GridPane.setConstraints(closeButton, 2, 2)
  children = Seq(face, mind, power, heart, weapon,
    okButton, closeButton)

  val tiles : Seq[EducateBeingTile] = Seq(face, mind, power, heart, weapon)

  def cards : Seq[Card] = tiles flatMap (_.card)

  def defaultPos(c : C) : CardDropTarget =
    c match {
      case C(_, Heart) => heart
      case C(_, Club) => power
      case C(_, Diamond) => mind
      case C(_, Spade) => weapon
    }

  def targets(c : C): Seq[CardDropTarget] =
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
