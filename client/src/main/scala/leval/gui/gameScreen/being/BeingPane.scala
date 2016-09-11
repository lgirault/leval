package leval.gui.gameScreen.being

import leval.core.{Card, CardOrigin, Club, Diamond, Heart, Spade, Suit}
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.geometry.Pos
import scalafx.scene.{Group, Node, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.{ImageView, WritableImage}
import scalafx.scene.input.MouseEvent
import scalafx.scene.text.{Text, TextAlignment}

/**
  * Created by Loïc Girault on 05/07/16.
  */
sealed abstract class Orientation {
  def posConstraints(s : Suit) : Node => Unit
}
case object Player extends Orientation {
  def posConstraints(s : Suit) : Node => Unit = s match {
    case Heart => BeingGrid.leftConstraints
    case Spade => BeingGrid.rightConstraints
    case Diamond => BeingGrid.topConstraints
    case Club => BeingGrid.bottomConstraints
  }
}
case object Opponent extends Orientation {
  def posConstraints(s : Suit) : Node => Unit = s match {
    case Heart => BeingGrid.rightConstraints
    case Spade => BeingGrid.leftConstraints
    case Diamond => BeingGrid.bottomConstraints
    case Club => BeingGrid.topConstraints
  }
}


class BeingResourcePane
(bp : BeingPane,
 val card : Card,
 val position : Suit)
 extends CardDropTarget {




  val backImg : Node = CardImg.back(Some(bp.cardHeight))

  def being = bp.being

  val eyeImg : Node = eyeImage(bp.cardWidth)
  eyeImg.visible = false
  val switchImg : Node = switchImage(bp.cardWidth)
  switchImg.visible = false

  val frontImg : Node = CardImg(card, Some(bp.cardHeight))

  val dmgTxt = new Text(""){
    style = "-fx-font-size: 24pt"
    textAlignment = TextAlignment.Center
    alignmentInParent = Pos.BottomCenter
    visible = false
  }


  def dmg : String = dmgTxt.text()
  def dmg_=(i : Int) = if(i == 0){
    dmgTxt.text = ""
    dmgTxt.visible = false
  } else{
    dmgTxt.text = i.toString
    dmgTxt.visible = true
  }



  import bp.{control,face}
  import bp.control.game

  var sCardDragAndDrop: Option[CardDragAndDrop] = bp.orientation match {
    case Player => Some(new BeingDragAndDrop(control, face, position))
    case Opponent => None
  }
  private [this] var subscription  : Option[Subscription] = None
  def unsetCardDragAndDrop() : Unit = {
    subscription foreach (_.cancel())
    subscription = None
  }

  def setCardDragAndDrap() : Unit = if(subscription.isEmpty){
    subscription = sCardDragAndDrop map (handleEvent(MouseEvent.Any)(_))
  }

  
  setCardDragAndDrap()

  def bonus = game.arcaneBonus(being, position)

  val bonusTxt = new Label(""){
    style =
      "-fx-font-size: 24pt;" +
      "-fx-background-color: white;"
    textAlignment = TextAlignment.Center
  }

  def bonusImage = {
    val scene = new Scene(new Group(bonusTxt))
    val img = new WritableImage(50,40)
    scene.snapshot(img)
    img
  }
  val bonusImageView =
  new ImageView {
    preserveRatio = true
    alignmentInParent = Pos.Center
    fitWidth = bp.cardWidth / 2
    visible = false
  }

  var frontSeq = Seq(frontImg, bonusImageView, dmgTxt, highlight)
  var backSeq = Seq(backImg, switchImg, eyeImg, bonusImageView, highlight)

  private [this] var reveal0 = false
  def reveal : Boolean = reveal0
  def reveal_=(b : Boolean) : Unit = {
    reveal0 = b
    children =
      if(b) frontSeq
      else backSeq
  }

  children = backSeq
  def looked : Boolean = eyeImg.visible()
  def looked_=(b : Boolean) : Unit = {
    eyeImg.visible = b
    switchImg.visible =
      if(b) false
      else switchImg.visible()
  }

  def switched : Boolean = switchImg.visible()
  def switched_=(b : Boolean) : Unit = {
    switchImg.visible = b
    eyeImg.visible =
      if(b) false
      else eyeImg.visible()
  }

  def onDrop(origin : CardOrigin) : Unit =
    control.playOnBeing(origin, bp.being, position)

  def update() : Unit = {
    bonusTxt.text = s"+$bonus"
    bonusImageView.image = bonusImage
    looked = game.lookedCards contains ((face, position))
    reveal = game.revealedCard contains ((face, position))
  }


  update()


  handleEvent(MouseEvent.MouseEntered) {
    me : MouseEvent =>
      bonusImageView.visible = true
  }

  handleEvent(MouseEvent.MouseExited) {
    me : MouseEvent =>
      bonusImageView.visible = false

  }
}

class BeingPane
( val control: GameScreenControl,
  val face : Card,
  val cardWidth : Double,
  val cardHeight : Double,
  val orientation: Orientation)
( implicit txt : ValText) extends BeingGrid {

  def being = control.game.beings(face)

  private [this] var resourcePanes0 = Map[Suit, BeingResourcePane]()
  def resourcePanes = resourcePanes0.values

  def resourcePane(s : Suit) : Option[BeingResourcePane] =
    resourcePanes0 get s


  import control.pane


  val educateButton : Node = educateImage(cardWidth/3)//closeCanvas(cardWidth)
  educateButton.visible = false
  educateButton.onMouseClicked = {
    me : MouseEvent =>
      pane.educateBeingPane.beingPane = BeingPane.this
  }
  BeingGrid.bottomRightCenterConstraints(educateButton)

  def placeResourcePane( c : Card, pos : Suit) : Unit ={
    val bpr = new BeingResourcePane(this, c, pos)
    resourcePanes0 += pos -> bpr
    orientation.posConstraints(pos)(bpr)
  }

  val faceImage = CardImg(face, Some(cardHeight))
  BeingGrid.centerConstraints(faceImage)

  def update(s : Suit) : Unit =
    (being.resources get s, resourcePane(s)) match {
    case (Some(c), None) => placeResourcePane(c, s)
    case (None, Some(rp)) =>
      children remove rp
      resourcePanes0 -= s
    case (Some(c), Some(rp)) =>
      if (c == rp.card) rp.update()
      else placeResourcePane(c, s) //after a switch from education
    case (None, None) => ()
  }


  def update() : Unit = {
    Suit.list foreach update
    children = educateButton +: faceImage +: resourcePanes.toSeq
  }

  update()

}
