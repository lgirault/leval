package leval.core

import leval.core.Game.StarIdx

import scala.collection.immutable.SortedSet

/**
  * Created by Loïc Girault on 20/06/16.
  */

object Being {
  type State = (Int, Int) // (Heart casualty, Power Casualty)

  implicit class StateOps(val state : State) extends AnyVal {
    def get(s : Suit) : Int = s match {
      case Heart => state._1
      case Club => state._2
      case _ => leval.error()
    }

    def add(s : Suit, i : Int) = {
      val (h, p) = state
      s match {
        case Heart => (h + i, p)
        case Club => (h, p + i)
        case _ => leval.error()
      }
    }
  }

}

case class Being
( owner : StarIdx,
  face : Card,
  resources : Map[Suit, Card],
  lover : Boolean = false,
  hasDrawn : Boolean = false //for Helios rule
 ){


  def -( s :Suit) = copy(resources = resources - s)
  def +( kv : (Suit, Card)) = copy(resources = resources + kv)

  def heart : Option[Card] = resources get Heart
  def weapon : Option[Card] = resources get Spade
  def mind : Option[Card] = resources get Diamond
  def power : Option[Card] = resources get Club

  def find(c : Card) : Option[Suit] =
    resources.toList find {
      case (_, `c`) => true
      case _ => false } map (_._1)


  //a being cannot be educated to become messianic or possessed so no ambiguity here
  def educateWith(card : C) : (Being, Card) = card match {
    case Card(King | Queen, _)
    | Card(Jack, Heart) => (copy(resources = resources + (Heart -> card), lover = true), resources(Heart))
    case Card(_ , Heart) => (copy(resources = resources + (Heart -> card), lover = false), resources(Heart))
    case Card(_, suit) => (copy(resources = resources + (suit -> card)), resources(suit))
  }

  def educateWith(e : Educate) : Being = e match {
    case Switch(`face`, c) => educateWith(c)._1

    case Rise(`face`, cards) =>
      val kvs = cards map (c => c.suit -> c)
      val b1 = copy(resources = kvs.foldLeft(resources)(_ + _), hasDrawn = false)
      b1.resources get Heart match {
        case Some(Card(King | Queen | Jack, _)) if ! b1.lover => b1.copy(lover = true)
        case _ => b1
      }
    case _ => this
  }

  def cards : List[Card] = face :: resources.values.toList

  def formationBonus(resource : Suit) = (resource, this) match {
    case (Heart, Formation(Child)) => 1
    case (Club, Formation(Fool)) => 1
    case (Club, Spectre(BlackLady)) => 2
    case (Club, Spectre(Royal)) => 3
    case (Diamond, Formation(Wizard) | Spectre(Royal|BlackLady)) => 1
    case (Spade, Formation(Knight)| Spectre(Royal)) => 1
    case _ => 0
  }

  def value(resource : Suit, v : Card => Int)  : Option[Int] = {
    resources get resource map {
      c =>
        val faceBonus = face match {
          case Joker(_) => v(face)
          case Card(_, suit) if suit == resource => v(face)
          case _ => 0
        }
        v(c) + faceBonus + formationBonus(resource)
    }
  }

  def firstDraw : Boolean = ! hasDrawn
}


object Formation {
  def explodedView(arg: Being): (Option[Card], Option[Card], Option[Card], Option[Card]) =
    (arg.heart, arg.weapon, arg.mind, arg.power)

  def unapply(being: Being) : Option[Formation] = unapply(explodedView(being))

  def unapply(arg: (Option[Card], Option[Card], Option[Card], Option[Card])): Option[Formation] = arg match {
    case (Some(_), None, None, None) => Some(Child)
    case (Some(_), Some(_), Some(_), Some(_)) => Some(Accomplished)
    case (Some(_), Some(_), None, Some(_)) => Some(Fool)
    case (None, Some(_), Some(_), Some(_)) => Some(Spectre)
    case (Some(_), None, Some(_), Some(_)) => Some(Wizard)
    case (Some(_), Some(_), Some(_), None) => Some(Knight)
    case _ => None
  }
}
sealed abstract class Formation
case object Accomplished extends Formation
case object Fool extends Formation
case object Knight extends Formation
case object Child extends Formation
case object Wizard extends Formation
case object Shadow extends Formation
case object Spectre extends Formation {
  def unapply(b: Being): Option[Spectre] = (b, b.lover, b.face) match {
    case (Formation(Spectre), false, _) => Some(Regular)
    case (Formation(Spectre), true, Card(King, _)) => Some(Royal)
    case (Formation(Spectre), true, Card(Queen, _)) => Some(BlackLady)
    case _ => None
  }
}

sealed abstract class Spectre
case object Regular extends Spectre
case object Royal extends Spectre
case object BlackLady extends Spectre

object Star {
  def emptyHand : Set[Card] = SortedSet.empty(Card.cardOrdering)

  def apply(id : PlayerId, hand : Seq[Card]) : Star = {
    new Star(id, 25, emptyHand ++ hand)
  }
}

case class Star
(id : PlayerId,
 majesty : Int,
 hand : Set[Card]){
  def name = id.name

  def +( i : Int) = copy(majesty = majesty + i)
  def -( i : Int) = copy(majesty = majesty - i)

  def +(c : Card) = copy(hand = hand + c)
  def -(c : Card) = copy(hand = hand - c)
  def ++(cs : Iterable[Card]) = copy(hand = hand ++ cs)
  def --(cs : Iterable[Card]) = copy(hand = hand -- cs)

}

