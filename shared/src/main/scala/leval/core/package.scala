package leval

import scala.util.Random

/**
  * Created by Loïc Girault on 20/06/16.
  */
package object core {

  type Deck = Seq[Card]

  val suits = Seq(Diamond, Club, Heart, Spade)
  val ranks = (for(i <- 1 to 10 ) yield Numeric(i)) ++ Seq(Jack, Queen, King)
  val jokers = Seq[Card]( Joker.Red, Joker.Black )

  def deck54() : Deck = {
    val cards = for {
      s <- suits
      r <- ranks
    } yield C(r, s)

    Random.shuffle(jokers ++ cards)
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
