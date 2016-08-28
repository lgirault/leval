package leval.gui.gameScreen

import leval.core.{ActPhase, InfluencePhase, Phase, SourcePhase}
import leval.gui.text.ValText

import scalafx.geometry.Pos
import scalafx.scene.{Group, Scene}
import scalafx.scene.control.Label
import scalafx.scene.image.{ImageView, WritableImage}

/**
  * Created by lorilan on 7/8/16.
  */
class StatusPane
(game : ObservableGame,
 width : Double)
(implicit txt : ValText) extends ImageView {

  style = "-fx-border-width: 1; -fx-border-color: black;"


  def update() : Unit = {
    val text =
      s"${txt.round} ${game.currentRound}\n" +
        game.currentStar.name+"\n" +
        phaseTxt(game.currentPhase)
    val w = CardImg.width * 1.5
    val h = (CardImg.height * 2) / 3
    val label = new Label(text) {
      minWidth = w
      minHeight = h
      maxWidth = w
      maxHeight = h
      prefWidth = w
      prefHeight = h
      alignment = Pos.Center
      wrapText = true
      style =
        "-fx-border-insets: 2;" +
          "-fx-font-size: 40;" +
          "-fx-text-alignment: center;"
    }

    val scene = new Scene(new Group(label))
    val img = new WritableImage(w.toInt, h.toInt)
    scene.snapshot(img)
    image = img
    preserveRatio = true
    fitWidth = width

  }

  update()

  def phaseTxt(p : Phase) : String =
    p match {
      case InfluencePhase(_) => txt.influence_phase
      case ActPhase(_) => txt.act_phase
      case SourcePhase => txt.source_phase
    }




}
