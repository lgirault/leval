package leval.gui.gameScreen

/**
  * Created by lorilan on 9/14/16.
  */
import leval.core.Card
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.control.{ButtonType, Dialog, DialogPane, Label}
import scalafx.scene.layout.{BorderPane, HBox, Pane}
import javafx.scene.{control => jfxsc}

import scalafx.geometry.Pos

/**
  * Created by lorilan on 7/9/16.
  */



class OSteinDialog
(parent: Pane,
 cards : Set[Card])
(implicit texts : ValText)
  extends Dialog[Card] {

  title = texts.draft

  val images : Seq[CardImageView] =
    cards.tail.foldLeft(List(CardImg(cards.head))) {
      case (acc, c) =>
        CardImg.cutLeft(c, 3) :: acc
    }

  var resultMap : Map[ButtonType, Card] = _

  dialogPane = new DialogPane(new jfxsc.DialogPane {
    resultMap = images.foldLeft(Map[ButtonType, Card]()) {
      case (m , imc) =>
        val bt = new ButtonType("")
        val b = createButton(bt).asInstanceOf[jfxsc.Button]
        imc.onMouseClicked = {
          evt =>
            if(evt.getClickCount == 2)
                b.fire()
        }
        m + (bt -> imc.card)

    }

  }) {
    content = new BorderPane {
     center = new HBox {
      children = images
    }
      bottom = new Label(texts.chose_card) {
        alignmentInParent = Pos.Center
        wrapText = true
      }
  }

  }

  resultConverter = resultMap.apply
  delegate.initOwner(parent.scene().getWindow)
}

