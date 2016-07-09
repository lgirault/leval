package leval.gui.gameScreen.being

import scalafx.scene.Node
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

/**
  * Created by lorilan on 7/9/16.
  */
abstract class BeingGrid extends GridPane {

  val cardHeight : Double
  val cardWidth : Double

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

  def centerConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 1, 1)

  def topConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 1, 0)

  def bottomConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 1, 2)

  def leftConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 0, 1)

  def rightConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 2, 1)
}
