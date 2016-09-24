package leval.gui.text

import leval.core.Rules

/**
  * Created by lorilan on 9/24/16.
  */
object ShowRules {

  def apply(r : Rules)(implicit texts : ValText) : String = {
    import r._
    val sb = new StringBuilder
    sb append coreRules
    if(ostein)
      sb append " O'Stein"

    if(allowMulligan) {
      sb append " "
      sb append texts.with_mulligan
    }

    if(nedemone)
      sb append " Nédémone"

    if(janus){
      sb append " Janus ("
      sb append texts.four_players
      sb append ")"
    }


    sb.toString()
  }
}
