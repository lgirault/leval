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
import scalafx.scene.shape.{Line, Rectangle}
import scalafx.scene.text.{Text, TextAlignment, TextFlow}

/**
  * Created by lorilan on 7/4/16.
  */

class CreateBeingTile
( val tile : Pane,
  doCardDragAndDrop: (Card, Origin) => CardDragAndDrop)
  extends CardDropTarget(tile) {

  var cardImg : Option[(CardImageView, CardDragAndDrop)] = None
  def card = cardImg map (_._1.card)

  highlight.handleEvent(MouseEvent.Any){
    me : MouseEvent =>
      cardImg.foreach {
        case (civ, cdad) => cdad(me)
      }
  }

  def onDrop(c : Card, origin : Origin) : Unit = {
    val ci = CardImg(c, tile.prefHeight.value)
    cardImg = Some((ci, doCardDragAndDrop(c, CreateBeingPanel)))
    tile.children.add(ci)
  }

}

class CreateBeingPane
(cardWidth : Double, cardHeight : Double,
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

    cv
  }

  def editMode(c : Card, origin: Origin) : Unit = {
    children = Seq(face, mind, power, heart, weapon, closeButton)
    defaultPos(c).onDrop(c, origin)
    open0 = true
  }

  def menuMode() : Unit = {
    children = createBeingLabel
    open0 = false
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

  //val button = closeButton
  GridPane.setConstraints(createBeingLabel, 1, 1)
  children = createBeingLabel
  val face = new CreateBeingTile(cardRectangle("Face"), doCardDragAndDrop)
  GridPane.setConstraints(face, 1, 1)
  val mind = new CreateBeingTile (cardRectangle("Mind"), doCardDragAndDrop)
  GridPane.setConstraints(mind, 1, 0)
  val power = new CreateBeingTile (cardRectangle("Power"), doCardDragAndDrop)
  GridPane.setConstraints(power, 1, 2)
  val heart = new CreateBeingTile (cardRectangle("Heart"), doCardDragAndDrop)
  GridPane.setConstraints(heart, 0, 1)
  val weapon = new CreateBeingTile (cardRectangle("Weapon"), doCardDragAndDrop)
  GridPane.setConstraints(weapon, 2, 1)

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

}
