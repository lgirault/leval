package leval.core

import scala.collection.immutable.SortedSet

/**
  * Created by Loïc Girault on 20/06/16.
  */

object Being {
  type State = (Int, Int) // (Heart casualty, Power Casualty)


}

case class Being
(face : FaceCard,
 resources : Map[Suit, Card],
 lover : Boolean = false){

  def this( face : FaceCard,
            resources : Seq[Card],
            lover : Boolean) =
    this(face, resources.foldLeft(Map[Suit, Card]()){
      case (m, c @ (r, s)) => m + (s -> c)
    }, lover)

  def heart : Option[Card] = resources get Heart
  def weapon : Option[Card] = resources get Spade
  def mind : Option[Card] = resources get Diamond
  def power : Option[Card] = resources get Club

  //a being cannot be educated to become messianic or possessed so no ambiguity here
  def educateWith(card : Card) : (Being, Option[Card]) = card match {
    case (King | Queen , _) => (copy(face, resources + (Heart -> card), lover = true), heart)
    case (_ , Heart) => (copy(face, resources + (Heart -> card), lover = false), heart)
    case (_, suit) => (copy(face, resources + (suit -> card)), resources get suit)
  }

  def cards : Seq[Card] = face +: resources.values.toSeq

  def regularFormationBonus(resource : Suit) = (resource, this) match {
    case (Heart, Formation(Child)) => 1
    case (Club, Formation(Fool)) => 1
    case (Diamond, Formation(Wizard)) => 1
    case (Spade, Formation(Knight)) => 1
    case _ => 0
  }
  def loverSpectreBonus(resource : Suit) = {
    val isSpectre = this match {
      case Formation(Spectre) => true
      case _ => false
    }

    if(isSpectre)
      (face.rank, resource) match {
        case (King | Queen, Diamond) => 1
        case (King, Club) => 3
        case (Queen, Club) => 2
        case (King, Spade) => 1
        case _ => 0
      }
    else 0
  }

  def formationBonus(resource : Suit) =
    if(lover) loverSpectreBonus(resource)
    else regularFormationBonus(resource)

  def value(resource : Suit, v : Card => Int) = {
    resources get resource map {
      c =>
        val faceBonus =
          if( face.suit == resource ) v(face)
          else 0
        v(c) + faceBonus + formationBonus(resource)
    }
  }
}


object Formation {
  def explodedView(arg: Being): (Option[Card], Option[Card], Option[Card], Option[Card]) =
    (arg.heart, arg.weapon, arg.mind, arg.power)

  def unapply(being: Being) : Option[Formation] = explodedView(being) match {
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


object Star {
  def apply(id : PlayerId, hand : Seq[Card]) : Star = {
    val s : Set[Card] = SortedSet.empty(Card.cardOrdering)
    new Star(id, 25, s ++ hand, Map())
  }
}

case class Star
(id : PlayerId,
 majesty : Int,
 hand : Set[Card],
 beings : Map[FaceCard, Being]) {

}

