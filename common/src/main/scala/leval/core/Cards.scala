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
case class Numeric(value: Int) extends Rank

//Cards keep their id deck
//its necessary because being maps are indexed by the card

sealed abstract class Card
case class C(deckId : Byte, rank: Rank, suit: Suit) extends Card


case class J(deckId : Byte, color : Joker.Color) extends Card
object Joker {
  sealed abstract class Color
  case object Red extends Color
  case object Black extends Color

  def unapply(c: Card): Option[Color] = c match {
    case j : J => Some(j.color)
    case _ => None
  }

}



object Card {

  def unapply(c: C): Option[(Rank, Suit)] = c match {
    case c1 : C => Some((c.rank, c.suit))
    case _ => None
  }

  def value(c : Card) : Int =
    c match {
      case Joker(_)
           | Card(Jack, _)=> 1
      case Card(Numeric(v), _) => v
      case Card(Queen, _) => 2
      case Card(King, _) => 3
    }


  private def orderingValue(c : Joker.Color) : Int = c match {
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
        case (Card(rx, sx), Card(ry, sy)) =>
          if (sx != sy)
            suitOrdering.compare(sx,sy)
          else rankOrdering.compare(rx, ry)


      }
  }
}