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
case object Ace extends Rank
case class Numeric(value: Int) extends Rank

sealed abstract class Card
case class C(rank: Rank, suit: Suit) extends Card

sealed abstract class Joker extends Card
object Joker {
  case object Red extends Joker
  case object Black extends Joker

  def unapply(c: Card): Option[Joker] = c match {
    case j : Joker => Some(j)
    case _ => None
  }

}



object Card {

  def value(c : Card) : Int =
    c match {
      case Joker(_) => 1
      case C(rank, _) => rank match {
        case Numeric(v) => v
        case Jack | Ace => 1
        case Queen => 2
        case King => 3
      }
    }


  private def orderingValue(c : Joker) : Int = c match {
    case Joker.Red => 0
    case Joker.Black  => 1
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

    def compare(x: Card, y: Card): Int =
      (x,y) match {
        case (Joker(cx), Joker(cy)) =>
          Ordering.Int.compare(orderingValue(cx), orderingValue(cy))
        case (Joker(_), _) => 1
        case (_, Joker(_)) => -1
        case (C(_, sx), C(_, sy)) => suitOrdering.compare(sx,sy)
      }
  }
}