package leval.gui

import com.typesafe.config.Config
import leval.core.Card
import leval.gui.text.{Eng, Fr, ValText}
import leval.LevalConfig._

import scalafx.Includes._
import scalafx.scene.control.{ButtonType, ComboBox, Dialog, Label}
import scalafx.scene.layout._

/**
  * Created by lorilan on 9/10/16.
  */
class ConfigDialog
(val cfg: Config,
 p : Pane) extends Dialog[Card] {

  val languages = new ComboBox[ValText](Seq(Fr, Eng)){
    value = cfg.lang()
  }

  def texts : ValText = languages.value()

  val ratios = new ComboBox[String](Seq("16:9", "4:3")){
    value = cfg getString Keys.screenRatio
  }


  def makeContent() = new GridPane {
    val leftColumn = new ColumnConstraints(150)
    val rightColumn = new ColumnConstraints(150)
    val rowCt = new RowConstraints(30)


    columnConstraints add leftColumn
    columnConstraints add rightColumn
    for (_ <- 1 to 2)
      rowConstraints add rowCt

    val nodes = Seq(Seq(new Label(texts.screen_ratio), ratios),
       Seq(new Label(texts.language), languages))

    for{
      (rowNodes, rowIdx) <- nodes.zipWithIndex
      (n, colIdx) <- rowNodes.zipWithIndex
    } GridPane.setConstraints(n, colIdx, rowIdx)


    children = nodes.flatten
  }

  languages.value.onChange {
    dialogPane().content = makeContent()
  }

  dialogPane().content = makeContent()
  delegate.initOwner(p.scene().getWindow)

  dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)

  def modifiedConfig() = {
    cfg.withAnyRefValue(Keys.screenRatio, ratios.value(): String)
        .withAnyRefValue(Keys.lang, texts.id)
  }
}