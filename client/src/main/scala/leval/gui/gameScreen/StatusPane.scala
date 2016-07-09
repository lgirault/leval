package leval.gui.gameScreen

import leval.core.{ActPhase, InfluencePhase, Phase, SourcePhase}
import leval.gui.text.ValText

import scalafx.geometry.Pos
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text

/**
  * Created by lorilan on 7/8/16.
  */
class StatusPane(implicit txt : ValText) extends VBox {


  private [this] val phaseTxt = new Text(txt.influence_phase)
  def phase_=(p : Phase) : Unit =
    phaseTxt.text = p match {
    case InfluencePhase(_) => txt.influence_phase
    case ActPhase(_) => txt.act_phase
    case SourcePhase => txt.source_phase
  }
  def phase : String = phaseTxt.text()

  private [this] val roundTxt = new Text(txt.round +" 1")
  def round_=(i : Int) : Unit =
    roundTxt.text = txt.round +" " + i
  def round: String = roundTxt.text()

  private [this] val starTxt = new Text(txt.round +" 1")

  def star_=(s : String) : Unit = starTxt.text = s
  def star: String = starTxt.text()

  alignment = Pos.Center
  spacing = 10
  children = Seq(
    roundTxt,
    starTxt,
    phaseTxt
  )
}
