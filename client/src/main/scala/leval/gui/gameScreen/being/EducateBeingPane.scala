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
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.StackPane


/**
  * Created by lorilan on 7/9/16.
  */


class EducateBeingTile
(val pane : EducateBeingPane,
 val pos : Suit)
  extends EditableBeingResourceTile {

  val switchImg : Node = switchImage(pane.cardWidth)
  switchImg.visible = false


  def doOnDrop(c : C) : Unit = {
    card = Some((c, true))
    this.children add switchImg
    pane.checkIfLegalAndUpdate()
  }

  def onDrop(origin : CardOrigin) : Unit =
  origin.card match {
    case c : C =>

    pane.sEducation match {
      case None =>
        if(pane.being.resources.get(pos).nonEmpty)
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
        if((pane.being.resources get pos ).nonEmpty)
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
 val hand : PlayerHandPane,
 val cardWidth : Double,
 val cardHeight : Double )
( implicit val txt : ValText )
  extends EditableBeingPane[EducateBeingTile] {

  style = "-fx-border-width: 3; " +
    "-fx-border-color: black; "

  alignmentInParent = Pos.Center

  private [this] var beingPane0 : BeingPane = _
  private [this] var position0 : Option[Int] = None
  def playerArea =  controller.pane.beingsPane(beingPane0.orientation)
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
    Seq(mind, power, heart, weapon) foreach (_.card = None)
  }

  def makeResourceTilePane(pos : Suit) =
    new EducateBeingTile(this, pos)


  val face = new StackPane {
    children = cardRectangle(txt.face, cardWidth, cardHeight)
  }
  centerConstraints(face)


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
    resources = b.resources
    b.resources foreach {
      case (suit, c) =>
        mapTiles(suit).card = Some((c, false))
    }
  }

  def being = beingPane0.being

  var sEducation : Option[Educate] = None


  okButton.onMouseClicked = {
    me : MouseEvent =>
      sEducation foreach (e =>
        controller.educate(e))
      resetBeingPane()

  }

  closeButton.onMouseClicked = {
    me : MouseEvent =>
      resetBeingPane()
      hand.update()
  }

  def faceCard = Some(being.face)

  children = Seq(face, mind, power, heart, weapon,
    okButton, closeButton)

  val rules = controller.game.rules

  def targets(c : C): Seq[CardDropTarget] ={

    val allowedTiles : Seq[CardDropTarget] = (mapTiles filter {
      case (pos, tile) => rules.validResource(being.face, resources,
        c, pos)
    } values).toList
    (being.face, c) match {
      case (Card(lover@(King | Queen), fsuit), Card(r : Face, s))
        if fsuit == s && rules.otherLover(lover) == r =>
        heart +: allowedTiles
      case _ => allowedTiles
    }
  }
  def legalFormation : Boolean =
    sEducation exists (e => rules.isValidBeing(being.educateWith(e)))
}
