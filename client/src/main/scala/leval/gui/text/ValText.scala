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

  val end_of_act_phase : String
  val every_being_has_acted : String

  val twilight_ceremony : String

  val educate : String
  val do_end_act_phase : String

  val face : String
  val mind : String
  val power : String
  val heart : String
  val weapon : String
  val create_being : String

  val burying : String
  val wait_end_burial : String
}

object Fr extends ValText {


  val influence_phase : String = "Phase d'influence"
  val act_phase : String = "Phase des actes"
  val source_phase : String = "Phase de la source"

  val round : String = "Tour"
  val majesty : String = "Majesté"

  val twilight_ceremony : String = "Cérémonie du crépuscule"

  val end_of_act_phase : String = "Fin de la phase des actes"
  val every_being_has_acted : String = "Tous les êtres ont agits"

  val educate : String = "Éduquer"
  val do_end_act_phase = "Terminer la phase des Actes"

  val face : String = "Figure"
  val mind : String = "Esprit"
  val power : String = "Pouvoir"
  val heart : String = "Cœur"
  val weapon : String = "Arme"
  val create_being : String = "Engendrer un être"

  val burying : String = "Enterrement"
  val wait_end_burial : String = "Attendez la fin de l'enterrement"
}

object Eng extends ValText {


  val influence_phase : String = "Influence phase"
  val act_phase : String = "Acts phase"
  val source_phase : String = "Source phase"

  val round : String = "Round"
  val majesty : String = "Majesty"

  val twilight_ceremony : String = "Twilight ceremony"

  val end_of_act_phase : String = "Fin de la phase des actes"
  val every_being_has_acted : String = "Tous les êtres ont agi"

  val educate : String = "Educate"
  val do_end_act_phase = "End Act phase"

  val face : String = "Face"
  val mind : String = "Mind"
  val power : String = "Power"
  val heart : String = "Heart"
  val weapon : String = "Weapon"
  val create_being : String = "Create being"

  val burying : String = "Burying"

  val wait_end_burial : String = "Wait end of burial"
}