package leval.gui.text

/**
  * Created by lorilan on 7/8/16.
  */
trait ValText {

  val influence_phase : String
  val act_phase : String
  val source_phase : String

  val round : String
  val majesty : String
}

object Fr extends ValText {


  val influence_phase : String = "Phase d'influence"
  val act_phase : String = "Phase des actes"
  val source_phase : String = "Phase de la source"

  val round : String = "Tour"
  val majesty : String = "Majesté"
}

object Eng extends ValText {


  val influence_phase : String = "Influence phase"
  val act_phase : String = "Acts phase"
  val source_phase : String = "Source phase"

  val round : String = "Round"
  val majesty : String = "Majesty"
}