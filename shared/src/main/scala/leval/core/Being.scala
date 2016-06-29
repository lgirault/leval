package leval.core

/**
  * Created by Loïc Girault on 20/06/16.
  */
case class Being
( head : FaceCard,
  heart : Option[Card],
  weapon : Option[Card],
  mind : Option[Card],
  power : Option[Card])

object Star {
  def apply(id : PlayerId, hand : Seq[Card]) : Star = new Star(id, 25, hand, Seq())
}
class Star
( val id : PlayerId,
  var majesty : Int,
  var hand : Seq[Card],
  var beeings : Seq[Being])

class Game
( val star1 : Star,
  val star2 : Star,
  var source : Deck,
  var deathRiver: Seq[Card])

object Game {

  def apply(s1 : Star, s2 : Star, src : Deck) = new Game(s1, s2, src, Seq())

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
}