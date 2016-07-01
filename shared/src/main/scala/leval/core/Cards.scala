package leval.core

/**
  * Created by Loïc Girault on 20/06/16.
  */
object Suit {
  val list = List(Diamond, Club, Heart, Spade)
}
sealed abstract class Suit
case object Diamond extends Suit//carreau
case object Club extends Suit//trefle
case object Heart extends Suit
case object Spade extends Suit //pique

sealed abstract class Rank
sealed abstract class Face extends Rank
case object Jack extends Face
case object Queen extends Face
case object King extends Face
case object Joker extends Face
case object Ace extends Rank
case class Numeric(value: Int) extends Rank

object Card {

  def value(c : Card) : Int = c.rank match {
    case Numeric(v) => v
    case Jack | Ace | Joker => 1
    case Queen => 2
    case King => 3
  }

  private def orderingValue(s : Suit) : Int = s match {
    case Diamond => 0
    case Club  => 1
    case Heart => 2
    case Spade => 3
  }

  private def orderingValue(r : Rank) : Int = r match {
    case Numeric(v) => v
    case Ace  => 1
    case Jack => 11
    case Queen => 12
    case King => 13
    case Joker => 14
  }

  implicit val suitOrdering = new Ordering[Suit] {
    def compare(x: Suit, y: Suit): Int =
      Ordering.Int.compare(orderingValue(x), orderingValue(y))
  }

  implicit val rankOrdering = new Ordering[Rank] {
    def compare(x: Rank, y: Rank): Int =
      Ordering.Int.compare(orderingValue(x), orderingValue(y))
  }

  implicit val cardOrdering = new Ordering[Card] {

    def suitBiasedCompare(x: (Rank, Suit), y: (Rank, Suit)): Int = {
      val c = suitOrdering.compare(x.suit, y.suit)
      if(c != 0) c
      else rankOrdering.compare(x.rank, y.rank)
    }

    def compare(x: (Rank, Suit), y: (Rank, Suit)): Int =
      (x,y) match {
        case ((Joker,_), (Joker, _)) =>
            suitOrdering.compare(x.suit, y.suit)
        case ((Joker,_), _) => 1
        case (_, (Joker, _)) => -1
        case _ => suitBiasedCompare(x,y)
      }
  }
}