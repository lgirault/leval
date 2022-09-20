package gp.leval.gamescreen.leftcolumn

import gp.leval.core.{Game, Phase}
import gp.leval.text.ValText
import gp.pixijs.{Container, DisplayObject, Text}
import gp.pixijs.TextStyle
import gp.leval.gamescreen.TextureDictionary


class StatusPane(game: Game)(using textures: TextureDictionary, txt: ValText):

  def view: DisplayObject =
    Container { root =>
      val text =
        if game.currentStarIdx == -1 then "" // during draft
        else s"""${txt.round} ${game.currentRound}
        |${game.currentStar.name}
        |${phaseTxt(game.currentPhase)}""".stripMargin

      //println(text)
      val style = new TextStyle
      style.fill = "red"
      root.addChild(new Text(text, style))

    }

  def phaseTxt(p: Phase): String =
    p match {
      case Phase.Influence(_) => txt.influence_phase
      case Phase.Act(_)       => txt.act_phase
      case Phase.Source       => txt.source_phase
    }
