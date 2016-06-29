package leval

import scala.util.Random

/**
  * Created by Loïc Girault on 20/06/16.
  */
package object core {
  type Card = (Rank, Suit)
  type FaceCard = (Face, Suit)

  implicit class CardOps(val c : Card) extends AnyVal {
    def rank : Rank = c._1
    def suit : Suit = c._2
  }



  type Deck = Seq[Card]

  val suits = Seq(Diamond, Club, Heart, Spade)
  val ranks = (for(i <- 2 to 10 ) yield Numeric(i)) ++ Seq(Jack, Queen, King, Ace)
  val jokers = Seq[Card]( (Joker, Heart), (Joker, Spade) )

  def deck54() : Deck = {
    val cards = for {
      s <- suits
      r <- ranks
    } yield (r, s)

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
