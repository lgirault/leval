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
(owner : StarIdx,
 face : Card,
 resources : Map[Suit, Card],
 lover : Boolean = false){

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
    case C(King | Queen , _) => (copy(resources = resources + (Heart -> card), lover = true), resources(Heart))
    case C(_ , Heart) => (copy(resources = resources + (Heart -> card), lover = false), resources(Heart))
    case C(_, suit) => (copy(resources = resources + (suit -> card)), resources(suit))
  }

  def educateWith(e : Educate) : Being = e match {
    case Switch(`face`, c) => educateWith(c)._1

    case Rise(`face`, cards) =>
      val kvs = cards map (c => c.suit -> c)
      copy(resources = kvs.foldLeft(resources)(_ + _))
    case _ => this
  }

  def cards : List[Card] = face :: resources.values.toList

  def formationBonus(resource : Suit) = (resource, this) match {
    case (Heart, Formation(Child)) => 1
    case (Club, Formation(Fool)) => 1
    case (Diamond, Formation(Wizard)) => 1
    case (Spade, Formation(Knight)) => 1
    case (_, Formation(Spectre)) if lover =>
      (face, resource) match {
        case (C(King | Queen,_), Diamond) => 1
        case (C(King,_), Club) => 3
        case (C(Queen,_), Club) => 2
        case (C(King,_), Spade) => 1
        case _ => 0
      }
    case _ => 0
  }

  def value(resource : Suit, v : Card => Int)  : Option[Int] = {
    resources get resource map {
      c =>
        val faceBonus = face match {
          case Joker(_) => v(face)
          case C(_, suit) if suit == resource => v(face)
          case _ => 0
        }
        v(c) + faceBonus + formationBonus(resource)
    }
  }
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
case object Spectre extends Formation
case object Shadow extends Formation

object Star {
  def apply(id : PlayerId, hand : Seq[Card]) : Star = {
    val s : Set[Card] = SortedSet.empty(Card.cardOrdering)
    new Star(id, 25, s ++ hand)
  }
}

case class Star
(id : PlayerId,
 majesty : Int,
 hand : Set[Card]){
  def name = id.name
}

