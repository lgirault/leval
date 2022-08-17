package leval

import scala.util.Random

/**
  * Created by Loïc Girault on 20/06/16.
  */
package object core {


  type Deck = Seq[Card]

  val suits = Seq(Diamond, Club, Heart, Spade)
  val ranks = (for(i <- 1 to 10 ) yield Numeric(i)) ++ Seq(Jack, Queen, King)
  def jokers(deckId : Byte) = {
    import Joker._
      Seq[Card](J(deckId, Red), J(deckId, Black))
  }
  def deck54(deckId : Byte = 0) : Deck = {
    val cards = for {
      s <- suits
      r <- ranks
    } yield C(deckId, r, s)

    Random.shuffle(jokers(deckId) ++ cards)
  }

  implicit class DeckOps(val d : Deck) extends AnyVal {
    def pick(n : Int) : (Deck, Seq[Card]) = {

      def aux( n : Int,  remaining : Deck, picked : Seq[Card] ) : (Deck, Seq[Card]) =
        if(n == 0 | remaining.isEmpty) (remaining, picked)
        else aux(n - 1, remaining.tail, remaining.head +: picked )

      aux(n, d, Seq())
    }
  }

}
