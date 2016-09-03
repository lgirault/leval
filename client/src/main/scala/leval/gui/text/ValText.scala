package leval.gui.text

import leval.core.{Club, Diamond, Heart, Spade, Suit}

import scalafx.scene.text.Font

/**
  * Created by lorilan on 7/8/16.
  */
object ValText {

  val defaultSize = 14d
  val fantasyFont =
    Font.loadFont(this.getClass.getResource("/fantasy1.ttf").toExternalForm, defaultSize)

  val defaultFont = new Font(Font.default.getName, defaultSize)

  /*
  style =
          "-fx-fill: red;"+
          "-fx-stroke: black;" +
          "-fx-stroke-width: 1;"
   */
}
trait ValText {

  val influence_phase : String
  val act_phase : String
  val source_phase : String

  val round : String
  val majesty : String

  val end_of_act_phase : String
  val every_beings_have_acted : String

  val twilight_ceremony : String

  val educate : String
  val do_end_phase : String

  val face : String
  val mind : String
  val power : String
  val heart : String
  val weapon : String

  def suitsText(s : Suit) = s match {
    case Diamond => mind
    case Club => power
    case Heart => heart
    case Spade => weapon
  }

  val create_being : String

  val burying : String
  val wait_end_burial : String

  val forbidden : String
  val cannot_attack_on_first_round : String

  val both_lose : String
  val wins : String
  val game_over : String

  val select_to_attack : String

  val information : String
  val owner_exit : String
  def disconnected( name : String) : String
}

object Fr extends ValText {


  val influence_phase : String = "Phase d'influence"
  val act_phase : String = "Phase des actes"
  val source_phase : String = "Phase de la source"

  val round : String = "Tour"
  val majesty : String = "Majesté"

  val twilight_ceremony : String = "Cérémonie du crépuscule"

  val end_of_act_phase : String = "Fin de la phase des actes"
  val every_beings_have_acted : String = "Tous les êtres ont agi"

  val educate : String = "Éduquer"
  val do_end_phase = "Terminer la phase"

  val face : String = "Figure"
  val mind : String = "Esprit"
  val power : String = "Pouvoir"
  val heart : String = "Cœur"
  val weapon : String = "Arme"
  val create_being : String = "Engendrer un être"

  val burying : String = "Enterrement"
  val wait_end_burial : String = "Attendez la fin de l'enterrement"

  val forbidden : String = "Interdit"
  val cannot_attack_on_first_round : String =
      "Vous ne pouvez attaquer au premier tour"

  val both_lose : String = "Levé de Soleil. Les deux étoiles perdent"
  val wins : String = " gagne !"
  val game_over : String = "Fin du jeu"

  val select_to_attack : String =
    "Sélectionner une carte ou l'étoile adverse pour attaquer"

  val information : String = "Info"

  val owner_exit : String = "Le créateur de la partie s'est déconnecté"

  def disconnected( name : String) : String =
    name + " s'est déconnecté(e)"
}

object Eng extends ValText {


  val influence_phase : String = "Influence phase"
  val act_phase : String = "Acts phase"
  val source_phase : String = "Source phase"

  val round : String = "Round"
  val majesty : String = "Majesty"

  val twilight_ceremony : String = "Twilight ceremony"

  val end_of_act_phase : String = "End of act phase"
  val every_beings_have_acted : String = "Every beings have acted"

  val educate : String = "Educate"
  val do_end_phase = "End phase"

  val face : String = "Face"
  val mind : String = "Mind"
  val power : String = "Power"
  val heart : String = "Heart"
  val weapon : String = "Weapon"
  val create_being : String = "Create being"

  val burying : String = "Burying"

  val wait_end_burial : String = "Wait end of burial"

  val forbidden : String = "Forbidden"
  val cannot_attack_on_first_round : String =
    "You cannot attack during the first round"

  val both_lose : String = "The sun rise. Both stars lose."
  val wins : String = " wins !"
  val game_over : String = "Game over"

  val select_to_attack : String =
    "Click on a card or the opponent star to attack"

  val information : String =
    "Info"

  val owner_exit : String =
    "The game owner as exited"

  def disconnected( name : String) : String =
    name + " is disconnected"
}