package gp.leval.text

import gp.leval.core.Suit

/**
  * Created by lorilan on 7/8/16.
  */
// object ValText {

//   val defaultSize = 14d
//   val fantasyFont =
//     Font.loadFont(this.getClass.getResource("/fantasy1.ttf").toExternalForm, defaultSize)

//   val defaultFont = new Font(Font.default.getName, defaultSize)

//   /*
//   style =
//           "-fx-fill: red;"+
//           "-fx-stroke: black;" +
//           "-fx-stroke-width: 1;"
//    */
// }
trait ValText {

  val id : String

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
    case Suit.Diamond => mind
    case Suit.Club => power
    case Suit.Heart => heart
    case Suit.Spade => weapon
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

  val card : String
  val draw_or_look : String
  val choose_next_effect : String
  def remainingEffects(collect : Int, look : Int) : String

  val collect_from_source : String
  val collect_from_river : String
  val look_resource : String
  val do_nothing : String

  def disconnected( name : String) : String

  val education : String
  val only_one_switch : String
  val switch_or_rise : String

  val screen_ratio : String
  val language : String

  val create_game : String
  val join_game : String
  val settings : String

  val unsupported : String
  val rules : String

  val connect : String
  val empty_login : String

  val draft : String
  val chose_card : String

  val allow_mulligan : String
  val draft_ongoing : String

  val with_mulligan : String
  val four_players : String
}

object Fr extends ValText {

  val id : String = "fr"
  override val toString = "Français"

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


  val card : String = "Carte"
  val draw_or_look : String = "Puiser ou regarder"
  val choose_next_effect : String = "Choisissez l'effet suivant : "
  def remainingEffects(collect : Int, look : Int) : String =
    s"(Puiser $collect carte(s), regarder $look carte(s)"

  val collect_from_source : String = "Puiser à la source"
  val collect_from_river : String = "Puiser à la rivière"
  val look_resource : String = "Regarder une ressource"
  val do_nothing : String = "Ne rien faire"

  val education : String = "Éducation"
  val only_one_switch : String = "Un seul échange à la fois"
  val switch_or_rise : String = "Échange ou élévation, pas les deux en même temps"

  val screen_ratio : String = "Format d'image"
  val language : String = "Langue"

  val create_game : String = "Créer une partie"
  val join_game : String = "Rejoindre une partie"
  val settings : String = "Options"

  val unsupported : String = "non supporté pour le moment"
  val rules : String = "Règle"
  val connect : String = "Connexion"
  val empty_login : String = "Identifiant vide"

  val draft = "Draft"
  val chose_card: String = "Choisissez une carte"

  val allow_mulligan : String = "Autoriser le mulligan"

  val draft_ongoing : String = "Draft en cours"

  val with_mulligan : String = "avec mulligan"
  val four_players : String = "4 joueurs"
}

object Eng extends ValText {

  val id : String = "eng"
  override val toString = "English"

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

  val card : String = "Card"
  val draw_or_look : String = "Draw or look Action"
  val choose_next_effect : String = "Choose next effect of action"
  def remainingEffects(collect : Int, look : Int) : String =
    s"(Draw $collect card(s), look $look card(s)"

  val collect_from_source : String = "Collect from source"
  val collect_from_river : String = "Collect from river"
  val look_resource : String = "Look a resource"
  val do_nothing : String = "Do nothing"


  val education : String = "Education"
  val only_one_switch : String = "Only one switch at a time"
  val switch_or_rise : String = "Switch or rise not both at the same time"

  val screen_ratio : String = "Screen format"

  val language : String = "Language"
  val create_game : String = "Create Game"
  val join_game : String = "Join Game"
  val settings : String = "Settings"

  val unsupported : String = "unsupported for now"
  val rules : String = "Rules"

  val connect : String = "Connect"
  val empty_login : String = "Empty login"

  val draft = "Draft"
  val chose_card: String = "Chose a card"

  val allow_mulligan : String = "Allow mulligan"
  val draft_ongoing : String = "Draft ongoing"

  val with_mulligan : String = "with mulligan"
  val four_players : String = "4 players"
}