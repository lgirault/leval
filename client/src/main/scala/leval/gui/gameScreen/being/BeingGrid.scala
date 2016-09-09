package leval.gui.gameScreen.being

import scalafx.scene.Node
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

/**
  * Created by lorilan on 7/9/16.
  */

object BeingGrid {
  def centerConstraints(n : Node) : Unit =
    GridPane.setConstraints(n, 3, 4)

  def topConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 3, 1)

  def bottomConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 3, 7)

  def leftConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 0, 4)

  def rightConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 6, 4)

  def bottomLeftCenterConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 1, 7)
  def bottomRightCenterConstraints( n : Node) : Unit =
    GridPane.setConstraints(n, 7, 7)
}
abstract class BeingGrid extends GridPane {

  val cardHeight : Double
  val cardWidth : Double

  val rowCts = new RowConstraints(cardHeight / 3)

  val colCts = new ColumnConstraints(cardWidth / 3)

  for (i <- 1 to 9) {
    rowConstraints.add(rowCts)
    columnConstraints.add(colCts)
  }


}
