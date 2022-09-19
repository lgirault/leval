package gp.leval.text

import gp.leval.core.Rules

/**
  * Created by lorilan on 9/24/16.
  */
object ShowRules:

  def apply(r : Rules)(using texts : ValText) : String =
    import r.*
    val sb = new StringBuilder
    sb append coreRules
    if ostein then
      sb append " O'Stein"

    if allowMulligan then 
      sb append " "
      sb append texts.with_mulligan
    

    if nedemone then
      sb append " Nédémone"

    if janus then
      sb append " Janus ("
      sb append texts.four_players
      sb append ")"
    

    sb.toString()

