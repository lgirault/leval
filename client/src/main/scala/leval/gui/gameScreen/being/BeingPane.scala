package leval.gui.gameScreen.being

import leval.core.{Being, Card, Club, Diamond, Heart, CardOrigin, Spade, Suit}
import leval.gui.gameScreen._
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.GridPane
import scalafx.scene.text.{Text, TextAlignment}

/**
  * Created by Loïc Girault on 05/07/16.
  */
sealed abstract class Orientation{
  val leftResource : Suit
  val rightResource : Suit
  val topResource : Suit
  val bottomResource : Suit
}
case object Player extends Orientation {
  val leftResource : Suit = Heart
  val rightResource : Suit = Spade
  val topResource : Suit = Diamond
  val bottomResource : Suit = Club
}
case object Opponent extends Orientation {
  val leftResource : Suit = Spade
  val rightResource : Suit = Heart
  val topResource : Suit = Club
  val bottomResource : Suit = Diamond
}


class BeingResourcePane
(bp : BeingPane,
 val card : Card,
 val position : Suit, // needed for lovers and Jokers
 sCardDragAndDrop: Option[CardDragAndDrop])
(val backImg : Node = CardImg.back(Some(bp.cardHeight)))
  extends CardDropTarget(backImg) {

  def being = bp.being

  val eyeImg : Node = eyeImage(bp.cardWidth)
  eyeImg.visible = false
  val switchImg : Node = switchImage(bp.cardWidth)
  switchImg.visible = false

  val frontImg : Node = CardImg(card, Some(bp.cardHeight))

  val dmgTxt = new Text("0"){
    style = "-fx-font-size: 24pt"
    textAlignment = TextAlignment.Center
    alignmentInParent = Pos.BottomCenter
  }

  def dmg : String = dmgTxt.text()
  def dmg_=(i : Int) = if(i == 0){
    dmgTxt.text = ""
    dmgTxt.visible = false
  } else{
    dmgTxt.text = i.toString
    dmgTxt.visible = true
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

  private [this] var reveal0 = false
  def reveal : Boolean = reveal0
  def reveal_=(b : Boolean) : Unit = {
    reveal0 = b
    children =
      if(b) Seq(frontImg, dmgTxt, highlight)
      else Seq(backImg, switchImg, eyeImg, highlight)
  }

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
    bp.control.playOnBeing(origin, bp.being, position)

  def update() : Unit = {
    import bp.control.game

    (game.beingsState get being.face, position) match {
      case (Some((heartDmg, _)), Heart) =>
        if(heartDmg >= game.value(being, Heart).get)
          bp remove Heart
        else
          dmg = heartDmg

      case (Some((_, powerDmg)), Club) =>
        if(powerDmg >= game.value(being, Club).get)
          bp remove Club
        else
          dmg = powerDmg
      case _ => dmg = 0

    }

    looked = game.lookedCards contains ((being.face, position))
    reveal = game.revealedCard contains ((being.face, position))
  }

}

class BeingPane
( val control: GameScreenControl,
  var being : Being,
  val cardWidth : Double,
  val cardHeight : Double,
  val orientation: Orientation)
( implicit txt : ValText) extends BeingGrid {


  private [this] var resourcePanes0 = Map[Suit, BeingResourcePane]()
  def resourcePanes = resourcePanes0.values

  def resourcePane(s : Suit) : Option[BeingResourcePane] = {
    resourcePanes0 get s
  }

  def remove(s : Suit) : Unit = {
    resourcePane(s) foreach (children remove _)
    resourcePanes0 -= s
    being = being.copy(resources = being.resources - s)
  }

  def update() : Unit = {
    resourcePanes foreach (_.update())
  }

  def update(s : Suit) : Unit = {
    resourcePane(s) foreach (_.update())
  }

  import control.pane.educateBeingPane


  val educateButton = new Button(txt.educate){
    visible = false
    onMouseClicked = {
      me : MouseEvent =>
        educateBeingPane.beingPane = BeingPane.this

    }
  }
  GridPane.setConstraints(educateButton, 2, 2)

  def placeResourcePane( c : Card, pos : Suit, place : Node => Unit) : Node ={
    val sCardDragAndDrop = orientation match {
      case Player => Some(new CardDragAndDrop(control,
        control.canDragAndDropOnActPhase(being.face),
        CardOrigin.Being(being, pos))(CardImg(c, front = false)))
      case Opponent => None
    }

    val bpr = new BeingResourcePane(this, c, pos, sCardDragAndDrop)()
    resourcePanes0 += pos -> bpr
    place(bpr)
    bpr
  }



  import orientation._
  def topCard  = being.resources get topResource
  def leftCard = being.resources get leftResource
  def rightCard = being.resources get rightResource
  def bottomCard  = being.resources get bottomResource

  def update(b : Being) : Unit = {
    being = b

    leftCard foreach { c =>
      placeResourcePane(c, leftResource, leftConstraints)
    }

    rightCard foreach { c =>
      placeResourcePane(c, rightResource, rightConstraints)
    }

    topCard foreach { c =>
      placeResourcePane(c, topResource, topConstraints)
    }

    bottomCard foreach { c =>
      placeResourcePane(c, bottomResource, bottomConstraints)
    }

    val faceImage = CardImg(being.face, Some(cardHeight))
    GridPane.setConstraints(faceImage, 1, 1)
    children = educateButton +: faceImage +: resourcePanes.toSeq
  }

  update(being)






}
