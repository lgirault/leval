package leval.gui.gameScreen

import javafx.geometry.Bounds

import leval.core.Card
import leval.gui.{CardImg, JFXCardImageView}
import CardDragAndDrop.NodeOps

import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{ColumnConstraints, GridPane, Pane}
import javafx.{scene => jfxs}

import scalafx.scene.Node
import scalafx.scene.control.{ButtonType, Dialog}
/**
  * Created by Loïc Girault on 12/07/16.
  */

object BurialPane {
  def switchColumn(n1 : Node , n2 : jfxs.Node): Unit = {
    val n1Idx = GridPane.getColumnIndex(n1)
    val n2Idx = GridPane.getColumnIndex(n2)
    GridPane.setColumnIndex(n1, n2Idx)
    GridPane.setColumnIndex(n2, n1Idx)
  }
  implicit class NodeFxOps(val n : jfxs.Node) extends AnyVal {
    def boundsInScene : Bounds =
      n.localToScene(n.boundsInLocal.value)
  }
}


class BurialDialog
(  cs : Seq[Card],
   cardWidth : Double,
   cardHeight : Double,
   p : Pane) extends Dialog[Seq[Card]] {
  //initOwner(window)
  title = "Card"
  headerText = "Choose order of burial"
  val pane = new BurialPane(cs, cardWidth, cardHeight)
  dialogPane().content = pane
  delegate.initOwner(p.scene().getWindow)
  resultConverter = {
    _ => pane.order
  }

  dialogPane().buttonTypes = Seq(ButtonType.OK)
}

import BurialPane.{switchColumn, NodeFxOps}
class BurialPane
( cards : Seq[Card],
  cardWidth : Double,
  cardHeight : Double)
  extends GridPane
  with (MouseEvent => Unit) {



  var anchorX : Double = 0
  var prevAnchorX : Double = 0
  var cardX : Double = 0

  val colWidth = cardWidth / 3

  val colCt = new ColumnConstraints(
    minWidth = colWidth,
    prefWidth = colWidth,
    maxWidth = colWidth)

  for( i <- 0 until cards.size + 2) {
    columnConstraints add colCt
  }

  val imgs = cards.zipWithIndex.map {
    case (c,idx) =>
      val img = CardImg(c, Some(cardHeight))
      img.handleEvent(MouseEvent.Any)(BurialPane.this)
      GridPane.setConstraints(img, idx, 0)
      img
  }.toArray


  children = imgs

  def apply(me : MouseEvent) : Unit  =
   me.source match {
     case civ : JFXCardImageView =>
        me.eventType match {
        case MouseEvent.MousePressed =>
          civ.managed = false
          anchorX = me.sceneX
          cardX = civ.boundsInScene.getMinX

        case MouseEvent.MouseReleased =>
          civ.managed = true

        case MouseEvent.MouseDragged =>
          prevAnchorX = anchorX
          anchorX = me.sceneX
          val tx = anchorX - prevAnchorX
          val pcx = cardX
          cardX += tx
          civ.x.value = civ.x.value + tx

          imgs foreach { i =>
            val lx = i.boundsInScene.getMinX
            if((cardX <= lx && pcx > lx ) ||
              (cardX >= lx && pcx < lx ))
              switchColumn(i, civ)
          }

          children = imgs.sortBy(GridPane.getColumnIndex)

        case _ => ()
      }
     case _=> ()
  }

  def order : Seq[Card] =
    imgs.sortBy(GridPane.getColumnIndex) map (_.card)
}
