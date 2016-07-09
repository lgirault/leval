package leval.gui.gameScreen

import leval.core.{Being, Card, Club, Diamond, Heart, Spade, Suit}
import leval.gui.CardImg

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}
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
case object Player extends Orientation{
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


object BeingResourcePane {
  val eyeUrl = this.getClass.getResource("/eye-icon.png").toExternalForm
  val eye = new Image(eyeUrl)
  def eyeImage(width : Double) =
    new ImageView(eye) {
      preserveRatio = true
      fitWidth = width
    }
}

class BeingResourcePane
(bp : BeingPane,
 val card : Card,
 val position : Suit, // needed for lovers and Jokers
 sCardDragAndDrop: Option[CardDragAndDrop])
(val backImg : Node = CardImg.back(Some(bp.cardHeight)))
  extends CardDropTarget(backImg) {

  val eyeImg : Node = BeingResourcePane.eyeImage(bp.cardWidth)
  eyeImg.visible = false
  val frontImg : Node = CardImg(card, bp.cardHeight)

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
  def unsetCardDragAndDrop() : Unit =
    subscription foreach (_.cancel())

  def setCardDragAndDrap() : Unit =
    subscription = sCardDragAndDrop map (handleEvent(MouseEvent.Any)(_))



  setCardDragAndDrap()

  private [this] var reveal0 = false
  def reveal : Boolean = reveal0
  def reveal_=(b : Boolean) : Unit = {
    reveal0 = b
    children =
      if(b) Seq(frontImg, dmgTxt, highlight)
      else
        Seq(backImg, eyeImg, highlight)
  }

  def looked : Boolean = eyeImg.visible()
  def looked_=(b : Boolean) : Unit = {
    eyeImg.visible = b
  }

  def onDrop(origin : Origin) : Unit =
    bp.control.playOnBeing(origin, bp.being, position)

  def update() : Unit = {
    import bp.control.game
    import bp.being

    (game.beingsState get being.face, position) match {
      case (Some((heartDmg, _)), Heart) =>
        dmg = heartDmg
      case (Some((_, powerDmg)), Club) =>
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
  val cardHeight : Double,
  val cardWidth : Double,
  val orientation: Orientation) extends GridPane {


  val rowCts = new RowConstraints(
    minHeight = cardHeight,
    prefHeight = cardHeight,
    maxHeight = cardHeight)

  val colCts = new ColumnConstraints(
    minWidth = cardWidth,
    prefWidth = cardWidth,
    maxWidth = cardWidth
  )

  for (i <- 0 until 3) {
    rowConstraints.add(rowCts)
    columnConstraints.add(colCts)
  }



  private [this] var resourcePanes0 = Seq[BeingResourcePane]()
  def resourcePanes = resourcePanes0

  def resourcePane(s : Suit) : Option[BeingResourcePane] = {
    resourcePanes0 find (_.position == s)
  }

  def update() : Unit = {
    resourcePanes foreach (_.update())
  }

  def update(s : Suit) : Unit = {
    resourcePane(s) foreach (_.update())
  }

  def placeResourcePane( c : Card, pos : Suit, place : Node => Unit) : Node ={

    val sCardDragAndDrop = orientation match {
      case Player => Some(new CardDragAndDrop(control,
        control.canDragAndDropOnActPhase(being.face),
        Origin.BeingPane(being, pos))(CardImg(c, front = false)))
      case Opponent => None
    }

    val bpr = new BeingResourcePane(this, c, pos, sCardDragAndDrop)()
    resourcePanes0 +:= bpr
    place(bpr)
    bpr
  }

  import orientation._
  def topCard  = being.resources get topResource
  def leftCard = being.resources get leftResource
  def rightCard = being.resources get rightResource
  def bottomCard  = being.resources get bottomResource

  leftCard foreach { c =>
    placeResourcePane(c, leftResource,
      GridPane.setConstraints(_, 0, 1))
  }

  rightCard foreach { c =>
    placeResourcePane(c, rightResource,
      GridPane.setConstraints(_, 2, 1))
  }

  topCard foreach { c =>
    placeResourcePane(c, topResource,
      GridPane.setConstraints(_, 1, 0))
  }

  bottomCard foreach { c =>
    placeResourcePane(c, bottomResource,
      GridPane.setConstraints(_, 1, 2))
  }

  val faceImage = CardImg(being.face, cardHeight)
  GridPane.setConstraints(faceImage, 1, 1)
  children = faceImage +: resourcePanes
}
