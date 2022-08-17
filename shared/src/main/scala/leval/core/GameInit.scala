package leval.core

/**
  * Created by lorilan on 9/2/16.
  */
case class GameInit
(twilight: Twilight,
 stars : Seq[Star], // for 4 or 3 players ??
 source : Deck,
 rules : Rules) extends Serializable {

  def game : Game = {
    val g = Game(rules.coreRules, stars, source)
    if(rules.ostein)
      g.copy(
        currentStarIdx = -1,
        currentPhase = InfluencePhase(-1))
    else g

  }


  def doTwilight : GameInit = {
    //tant qu'on a pas deux cartes égales, on continue de piocher (le val 1, p 20)
    val (d, Seq(h1, h2)) = GameInit.doTwilight(source)

    val Seq(s01, s02) = stars
    val (s1, s2) = (s01 ++ h1, s02 ++ h2)

    if(Card.value(h1.head) > Card.value(h2.head))
      copy(Twilight(Seq(h1, h2)), stars = Seq(s1, s2), source = d)
    else
      copy(Twilight(Seq(h2, h1)), stars = Seq(s2, s1), source = d)
  }
}

object GameInit {

  def apply(players : Seq[PlayerId], rule : Rules) : GameInit = players match {
    case p1 +: p2 +: Nil => this.apply(p1, p2, rule)
    case _ => leval.error("two players only")
  }
  def apply(pid1 : PlayerId, pid2 : PlayerId, rules : Rules) : GameInit = {
    val deck = deck54()

    import rules.{coreRules => crules}
    // on pioche 9 carte
    val (d2, hand1) = deck.pick(9)
    val (d3, hand2) = d2.pick(9)

    new GameInit(Twilight(Seq()),
      Seq(Star(pid1, crules.startingMajesty, hand1),
        Star(pid2, crules.startingMajesty, hand2)),
      d3, rules)
  }



  def hasFace(h : Set[Card]) =
    h.exists {
      case Joker(_) => true
      case Card(King|Queen|Jack, _) => true
      case _ => false
    }
  def mulligan(g : Game) : Boolean =
    g.stars.exists (s => ! hasFace(s.hand))


  def gameWithoutMulligan(players : Seq[PlayerId], rules: Rules) : GameInit = {
    val gi = GameInit(players, rules).doTwilight
    if(mulligan(gi.game)) gameWithoutMulligan(players, rules)
    else gi
  }

  def doTwilight(source : Seq[Card]) : (Seq[Card], Seq[Seq[Card]]) = {
    var d = source
    var h1 = Seq(d.head)
    d = d.tail
    var h2 = Seq(d.head)
    d = d.tail

    while(Card.value(h1.head) == Card.value(h2.head)) d match {
      case c1 +: c2 +: remainings =>
        d = remainings
        h1 = c1 +: h1
        h2 = c2 +: h2

      case Nil | Seq(_) => leval.error()
    }
    (d, Seq(h1, h2))
  }
}