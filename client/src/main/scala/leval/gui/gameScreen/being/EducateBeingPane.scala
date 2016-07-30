package leval.gui.gameScreen.being

import leval.ignore
import leval.core._
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.image.ImageView
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{GridPane, Pane, StackPane}


/**
  * Created by lorilan on 7/9/16.
  */


class EducateBeingTile
(val pane : EducateBeingPane,
 val backImg : Pane,
 val hand : PlayerHandPane,
 val suit : Suit)
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
  def card_=(c : Card) = setCard(c, front = true)

  def setCard(c : Card, front : Boolean) ={
    card0 = Some(c)
    cardImg = Some(CardImg(c, Some(backImg.prefHeight.value), front))
  }

  def reset() = {
    card0 = None
    cardImg = None
  }

  def doOnDrop(c : C) : Unit = {
    card = c
    if(pane.legalFormation)
      pane.okButton.visible = true
    else
      pane.okButton.visible = false

    hand.update(pane.cards)
  }

  def onDrop(origin : CardOrigin) : Unit =
  origin.card match {
    case c : C =>

    pane.sEducation match {
      case None =>
        if(cardImg0.nonEmpty)
          pane.sEducation = Some(Switch(pane.being.face, c))
        else
          pane.sEducation = Some(Rise(pane.being.face, Seq(c)))
        doOnDrop(c)
      case Some(Switch(tgt, c2)) if c2.suit == c.suit =>
        pane.sEducation = Some(Switch(tgt, c2))
        doOnDrop(c2)
      case Some(Switch(tgt, c2))  =>
        ignore(new Alert(AlertType.Information) {
          delegate.initOwner(pane.scene().getWindow)
          title = "Education"
          headerText = s"Only one switch at a time"
        }.showAndWait())

      case Some(Rise(tgt, s)) =>
        if((pane.being.resources get suit ).nonEmpty)
          ignore(new Alert(AlertType.Information) {
            delegate.initOwner(pane.scene().getWindow)
            title = "Education"
            headerText = s"Switch or rise not both at the same time"
          }.showAndWait())
        else {
          val newS = c +: s.filterNot(_.suit == c.suit)
          pane.sEducation = Some(Rise(tgt, newS))
          doOnDrop(c)
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
    "-fx-background-color : blue;"

  alignmentInParent = Pos.Center

  private [this] var beingPane0 : BeingPane = _
  private [this] var position0 : Option[Int] = None
  def playerArea =   controller.pane.beingsPane(beingPane0.orientation)
  def position = position0
  def position_=(p : Option[ Int]) = {
    position0 = p
    p.foreach { idx => playerArea.children.set(idx, this) }
  }

  def resetBeingPane() : Unit = {
    position foreach {
      idx =>
        playerArea.children.set(idx, beingPane)
        position0 = None
    }
    sEducation = None
    Seq(mind, power, heart, weapon) foreach (_.reset())
  }



  def makeTilePane(txt : String, s : Suit) =
    new EducateBeingTile(this,
      cardRectangle(txt, cardWidth, cardHeight),
      hand, s)

  val face = new StackPane{
    children = cardRectangle(txt.face, cardWidth, cardHeight)
  } //makeTilePane(txt.face)
  centerConstraints(face)
  val mind = makeTilePane(txt.mind, Diamond)
  topConstraints(mind)
  val power = makeTilePane(txt.power, Club)
  bottomConstraints(power)
  val heart = makeTilePane(txt.heart, Heart)
  leftConstraints(heart)
  val weapon = makeTilePane(txt.weapon, Spade)
  rightConstraints(weapon)


  private [this] def installEducatePane(bp : BeingPane) : Boolean = {

    val siblings = controller.pane.beingsPane(bp.orientation).children
    val index = siblings.indexOf(bp)
    if(index != -1) {
      position = Some(index)
      true
    }
    else
      false
  }

  def isOpen = position.nonEmpty

  def beingPane = beingPane0
  def beingPane_=(bp : BeingPane) = {
    beingPane0 = bp
    being = bp.being
    if(!installEducatePane(bp)){
        leval.error()
    }
  }
  def being_=(b : Being) : Unit = {
    face.children = CardImg(b.face, Some(cardHeight))
    b.mind foreach (mind.setCard(_, front = false))
    b.heart foreach (heart.setCard(_, front = false))
    b.power foreach (power.setCard(_, front = false))
    b.weapon foreach (weapon.setCard(_, front = false))

  }

  def being = beingPane0.being

  var sEducation : Option[Educate] = None

  val okButton = okCanvas(cardWidth)
  okButton.visible = false
  okButton.onMouseClicked = {
    me : MouseEvent =>
      sEducation foreach (e =>
        controller.educate(e))
      resetBeingPane()

  }

  val closeButton : Node = closeCanvas(cardWidth)
  closeButton.onMouseClicked = {
    me : MouseEvent =>
      resetBeingPane()
      hand.update()
  }

  GridPane.setConstraints(okButton, 0, 2)
  GridPane.setConstraints(closeButton, 2, 2)
  children = Seq(face, mind, power, heart, weapon,
    okButton, closeButton)

  val tiles : Seq[EducateBeingTile] = Seq(mind, power, heart, weapon)
  def cards : Seq[Card] = tiles flatMap (_.card)
  val mapTiles : Map[Suit, EducateBeingTile] =
    Map(Diamond -> mind,
      Club -> power,
      Heart -> heart,
      Spade -> weapon)



  val rules = controller.game.rules

  def targets(c : C): Seq[CardDropTarget] ={

    val allowedTiles : Seq[CardDropTarget] = (mapTiles filter {
      case (pos, tile) => rules.validResource(being.face, c, pos)
    } values).toList
    (being.face, c) match {
      case (C(lover@(King | Queen), fsuit), C(r : Face, s))
        if fsuit == s && rules.otherLover(lover) == r =>
        heart +: allowedTiles
      case _ => allowedTiles
    }
  }
  def legalFormation : Boolean =
    sEducation exists (e => rules.validBeing(being.educateWith(e)))
}
