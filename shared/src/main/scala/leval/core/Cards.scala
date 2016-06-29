package leval.core

/**
  * Created by Loïc Girault on 20/06/16.
  */
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
}