package leval.gui.gameScreen.being

import scalafx.scene.Node
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

/**
  * Created by lorilan on 7/9/16.
  */
abstract class BeingGrid extends GridPane {

  val cardHeight : Double
  val cardWidth : Double

  val rowCts = new RowConstraints(cardHeight / 3)

  val colCts = new ColumnConstraints(cardWidth / 3)

  for (i <- 0 until 9) {
    rowConstraints.add(rowCts)
    columnConstraints.add(colCts)
  }

  def centerConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 3, 3)

  def topConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 3, 0)

  def bottomConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 3, 6)

  def leftConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 0, 3)

  def rightConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 6, 3)

  def bottomLeftCenterConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 1, 6)
  def bottomRightCenterConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 7, 6)
}
