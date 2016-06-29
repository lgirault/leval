package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
class Game
( val stars : Array[Star], // for 4 or 3 players ??
  val currentPlayer : Int,
  val roundState: RoundState,
  val beingsState : Map[Being, Being.State], //reset each round
  val source : Deck,
  val deathRiver: Seq[Card])


object Game {

  def apply(s1 : Star, s2 : Star, src : Deck) =
    new Game(Array(s1, s2), 0, InfluencePhase, Map(), src, Seq())

  def apply(pid1 : PlayerId, pid2 : PlayerId) : Game = {
    val deck = deck54()

    // on pioche 9 carte + 1 qui sert à déterminer qui commence
    val (d2, hand1) = deck.pick(10)
    val (d3, hand2) = d2.pick(10)

    //tant qu'on a pas deux carte différente, on continue de piocher (le val 1, p 20)
    var h1 = hand1
    var h2 = hand2
    var d = d3
    while(Card.value(h1.head) == Card.value(h2.head)) d match {
      case c1 +: c2 +: remainings =>
        d = remainings
        h1 = c1 +: h1
        h2 = c2 +: h2
      case Nil | Seq(_)=> ???
    }

    val (s1, s2) =
      if(Card.value(h1.head) > Card.value(h2.head))
        (Star(pid1, h1), Star(pid2, h2))
      else
        (Star(pid2, h2), Star(pid1, h1))

    Game(s1, s2, d)

  }

  def effect(card : Card/*, target : ???*/) : Game = ???

  def effect(card : Card, target : Star) : Star = card.suit match {
    case Heart =>
      target.copy(majesty = target.majesty + Card.value(card))
    case Diamond | Spade =>
      target.copy(majesty = target.majesty - Card.value(card))
    case Club => target
  }



}
