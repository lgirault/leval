package gp.leval.core

import scala.util.Random

/** Created by Loïc Girault on 20/06/16.
  */
type Deck = List[Card]

val suits = List(Suit.Diamond, Suit.Club, Suit.Heart, Suit.Spade)
val ranks = (for (i <- 1 to 10) yield Rank.Numeric(i)) ++ List(Rank.Jack, Rank.Queen, Rank.King)
def jokers(deckId: Byte) = {
  import Joker.*
  List[Card](Card.J(deckId, Red), Card.J(deckId, Black))
}
def deck54(deckId: Byte = 0): Deck = {
  val cards = for {
    s <- suits
    r <- ranks
  } yield Card.C(deckId, r, s)

  Random.shuffle(jokers(deckId) ++ cards)
}

implicit class DeckOps(val d: Deck) extends AnyVal {
  def pick(n: Int): (Deck, List[Card]) = {

    def aux(n: Int, remaining: Deck, picked: List[Card]): (Deck, List[Card]) =
      if n == 0 | remaining.isEmpty then (remaining, picked)
      else aux(n - 1, remaining.tail, remaining.head +: picked)

    aux(n, d, List())
  }
}
