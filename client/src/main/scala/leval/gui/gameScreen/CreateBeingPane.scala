package leval.gui.gameScreen

import leval.core._

import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Line, Rectangle}
import scalafx.scene.text.{Text, TextAlignment, TextFlow}

/**
  * Created by lorilan on 7/4/16.
  */
class CreateBeingPane(cardWidth : Double, cardHeight : Double) extends GridPane {

  def cardRectangle(txt: String): Node = new StackPane {
    val rect = Rectangle(cardWidth, cardHeight, Color.White)
    rect.setStroke(Color.Green)
    rect.setArcWidth(20)
    rect.setArcHeight(20)
    val txtFlow = new TextFlow (new Text(txt)) {
      textAlignment = TextAlignment.Center
    }
    children = Seq(rect, txtFlow)
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

    //    val rect = Rectangle(cardWidth / 4, cardWidth / 4, Color.White)
//    rect.setStroke(Color.Green)
//    rect.setArcWidth(5)
//    rect.setArcHeight(5)
//    val b = rect.boundsInParent.value
//    val l1 = Line(b.get)
    cv
  }

  def editMode() : Unit = {
    children = Seq(face, mind, power, heart, weapon, closeButton)
  }

  def menuMode() : Unit = {
    children = createBeingLabel
  }

  val createBeingLabel =
    new CardDropTarget(cardRectangle("Create Beeing")) {
      def onDrop(c: Card, origin: Origin) = {
        editMode()
      }
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
  val face = new CardDropTarget (cardRectangle("Face")){
    def onDrop(c : Card, origin : Origin) = {
    }
  }
  GridPane.setConstraints(face, 1, 1)
  val mind = new CardDropTarget (cardRectangle("Mind")){
    def onDrop(c : Card, origin : Origin) = {
    }
  }
  GridPane.setConstraints(mind, 1, 0)
  val power = new CardDropTarget (cardRectangle("Power")){
    def onDrop(c : Card, origin : Origin) = {
    }
  }
  GridPane.setConstraints(power, 1, 2)
  val heart = new CardDropTarget (cardRectangle("Heart")){
    def onDrop(c : Card, origin : Origin) = {
    }
  }
  GridPane.setConstraints(heart, 0, 1)
  val weapon = new CardDropTarget (cardRectangle("Weapon")){
    def onDrop(c : Card, origin : Origin) = {
    }
  }
  GridPane.setConstraints(weapon, 2, 1)

  GridPane.setConstraints(closeButton, 2, 2)
  closeButton.alignmentInParent = Pos.BottomRight



}
