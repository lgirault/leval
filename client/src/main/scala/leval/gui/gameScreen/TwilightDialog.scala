package leval.gui.gameScreen

import leval.core.Twilight
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog}
import scalafx.scene.layout.{ColumnConstraints, GridPane, HBox, VBox}
import scalafx.scene.text.Text

/**
  * Created by lorilan on 7/9/16.
  */
class TwilightDialog
(control: GameScreenControl,
 twilight : Twilight)
(implicit txt : ValText)
  extends Dialog[Unit] {

  title = txt.twilight_ceremony

  val cts = new ColumnConstraints{
    percentWidth = 50
  }

  val t1 = twilight.cards(control.playerGameIdx)

  import control.game
  val s1 = new VBox {
    children = Seq(new Text(game.stars(control.playerGameIdx).name),
      new HBox{
        children add CardImg(t1.head)
        t1.tail foreach (c => children.add(CardImg.cutRight(c, 3)))
      }
    )
  }

  val t2 = twilight.cards(control.opponentId)

  import control.game
  val s2 = new VBox {
    children = Seq(new Text(game.stars(control.opponentId).name),
      new HBox{
        val imgs = CardImg(t2.head) +: (t2.tail map (CardImg.cutLeft(_, 3)))
        imgs.reverse foreach (children add _)
      }
    )
  }

  GridPane.setConstraints(s1, 0, 0)
  GridPane.setConstraints(s2, 1, 0)

  dialogPane().content = new GridPane {
    columnConstraints add cts
    columnConstraints add cts
    children = Seq(s1,s2)

  }
  delegate.initOwner(control.pane.scene().getWindow)


  dialogPane().buttonTypes = Seq(ButtonType.OK)
}
