package leval.gui.gameScreen

import leval.core._
import leval.gui.{CardImageView, CardImg}

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Text, TextAlignment, TextFlow}

/**
  * Created by lorilan on 7/4/16.
  */

class CreateBeingTile
(val pane : CreateBeingPane,
 val tile : Pane,
 val hand : PlayerHandPane,
 doCardDragAndDrop: (Card, Origin) => CardDragAndDrop )
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
    val ci = CardImg(c, tile.prefHeight.value)
    cardImg = Some((ci, doCardDragAndDrop(c, Origin.CreateBeingPane)))
  }

  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      cardImg.foreach {
        case (civ, cdad) => cdad(me)
      }
  }

  def onDrop(c : Card, origin : Origin) : Unit = {
    card = c
    if(pane.legalFormation)
      pane.okButton.visible = true
    else
      pane.okButton.visible = false

    hand.update(pane.cards)
  }
}

class CreateBeingPane
(controller : GameScreenControl,
 hand : PlayerHandPane,
 cardWidth : Double, cardHeight : Double,
 doCardDragAndDrop: (Card, Origin) => CardDragAndDrop) extends GridPane {

  def cardRectangle(txt: String): Pane = new StackPane {
    val rect = Rectangle(cardWidth, cardHeight, Color.White)
    prefHeight = cardHeight
    rect.setStroke(Color.Green)
    rect.setArcWidth(20)
    rect.setArcHeight(20)
    val txtFlow = new TextFlow (new Text(txt)) {
      textAlignment = TextAlignment.Center
    }
    children = Seq(rect, txtFlow)
  }

  private [this] var open0 : Boolean = false

  def isOpen = open0

  val okButton : Node = {
    val w = cardWidth / 4
    val cv = new Canvas(1.3*w, w+3)
    val gc = cv.graphicsContext2D
    //gc.fill = Color.White
    gc.stroke = Color.Green
    gc.strokeLine(1, 1, 1, w) // top -
    gc.strokeLine(1, 1, w, 1) // left |
    gc.strokeLine(w, 1, w, w) // right |
    gc.strokeLine(1, w, w, w) // bottom -

    gc.strokeLine(0, w/3, w/2, w) // \
    gc.strokeLine(w/2, w, 1.25*w, w/5) // /

    cv.visible = false

    cv.onMouseClicked = {
      me : MouseEvent =>
        being foreach controller.placeBeing
    }

    cv
  }

  def editMode(c : Card, origin: Origin) : Unit = {
    children = Seq(face, mind, power, heart, weapon, okButton, closeButton)
    defaultPos(c).onDrop(c, origin)
    open0 = true
  }

  def menuMode() : Unit = {
    children = createBeingLabel
    open0 = false
    tiles.foreach {
      _.cardImg = None
    }
    hand.update(Seq())
  }

  val createBeingLabel =
    new CardDropTarget(cardRectangle("Create Beeing")) {
      def onDrop(c: Card, origin: Origin) =
        editMode(c, origin)
    }


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

  def makeTilePane(txt : String) =
    new CreateBeingTile(this, cardRectangle(txt), hand, doCardDragAndDrop)

  GridPane.setConstraints(createBeingLabel, 1, 1)
  children = createBeingLabel
  val face = makeTilePane("Face")
  GridPane.setConstraints(face, 1, 1)
  val mind = makeTilePane("Mind")
  GridPane.setConstraints(mind, 1, 0)
  val power = makeTilePane("Power")
  GridPane.setConstraints(power, 1, 2)
  val heart = makeTilePane("Heart")
  GridPane.setConstraints(heart, 0, 1)
  val weapon = makeTilePane("Weapon")
  GridPane.setConstraints(weapon, 2, 1)


  val tiles : Seq[CreateBeingTile] = Seq(face, mind, power, heart, weapon)

  def cards : Seq[Card] = tiles flatMap (_.card)

  def being : Option[Being] = {
    face.card map {
      case fc : FaceCard => new Being(fc,
        cards.tail,
        heart.card exists (_.rank match {
          case King | Queen => true
          case _ => false
        })
      )
      case _ => leval.error()
    }
  }


  val closeButton : Node = {
    val w = cardWidth / 4
    val cv = new Canvas(w+3, w+3)
    val gc = cv.graphicsContext2D
    //gc.fill = Color.White
    gc.stroke = Color.Green
    gc.strokeLine(1, 1, 1, w) // top -
    gc.strokeLine(1, 1, w, 1) // left |
    gc.strokeLine(w, 1, w, w) // right |
    gc.strokeLine(1, w, w, w) // bottom -

    gc.strokeLine(1, 1, w, w) // \
    gc.strokeLine(w, 1, 1, w) // /

    cv.onMouseClicked = {
      me : MouseEvent =>
        menuMode()

    }

    cv
  }

  GridPane.setConstraints(okButton, 2, 2)
  okButton.alignmentInParent = Pos.BottomCenter

  GridPane.setConstraints(closeButton, 2, 2)
  closeButton.alignmentInParent = Pos.BottomRight



  def defaultPos(c : Card) : CardDropTarget =
    c match {
      case (_ : Face, _) => face
      case (_, Heart) => heart
      case (_, Club) => power
      case (_, Diamond) => mind
      case (_, Spade) => weapon
    }

  def targets(c : Card ): Seq[CardDropTarget] =
    defaultPos(c) +: (
      face.card map {
        fc =>
          (fc, c) match {
            case ((Queen, fsuit), (King, suit)) if fsuit == suit =>
              Seq(heart)
            case ((King, fsuit), (Queen, suit)) if fsuit == suit =>
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
