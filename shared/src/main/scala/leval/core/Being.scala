package leval.core

import scala.collection.immutable.SortedSet

/**
  * Created by Loïc Girault on 20/06/16.
  */

object Being {
  type State = (Int, Int) // (Heart casualty, Power Casualty)
  def unapply(arg: Being): Some[(FaceCard, Option[Card], Option[Card], Option[Card], Option[Card])] =
    Some((arg.face, arg.heart, arg.weapon, arg.mind, arg.power))

}

class Being
(val face : FaceCard,
 val resources : Map[Suit, Card]){

  def this( face : FaceCard,
            resources : Seq[Card]) =
    this(face, resources.foldLeft(Map[Suit, Card]()){
      case (m, c @ (r, s)) => m + (s -> c)
    })

  def heart : Option[Card] = resources get Heart
  def weapon : Option[Card] = resources get Spade
  def mind : Option[Card] = resources get Diamond
  def power : Option[Card] = resources get Club

  def copy(face : FaceCard = face,
           resources : Map[Suit, Card] = resources) : Being =
    new Being(face, resources)

  //a being cannot be educated to become messianic or possessed so no ambiguity here
  def educateWith(card : Card) : (Being, Option[Card]) = card match {
    case (King | Queen , _) => (new Lover(face, resources + (Heart -> card)), heart)
    case (_, suit) => (new Being(face, resources + (suit -> card)), resources get suit)
  }

  def cards : Seq[Card] = face +: resources.values.toSeq

  def formationBonus(resource : Suit) = (resource, this) match {
    case (Heart, Formation(Child)) => 1
    case (Club, Formation(Fool)) => 1
    case (Diamond, Formation(Wizard)) => 1
    case (Spade, Formation(Knight)) => 1
    case _ => 0
  }

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

//separate class to contextualize the spectre and infer Dark Lady or Spectre Royal
class Lover
( face : FaceCard,
  resources : Map[Suit, Card]) extends Being(face, resources){

  def this( face : FaceCard,
            resources : Seq[Card]) =
    this(face, resources.foldLeft(Map[Suit, Card]()){
      case (m, c @ (r, s)) => m + (s -> c)
    })

  override def copy(face : FaceCard = face,
                    resources : Map[Suit, Card] = resources) : Being =
    new Lover(face, resources)


  override def educateWith(card : Card) : (Being, Option[Card]) = card match {
    case (_, Heart) => (new Being(face, resources + (Heart -> card)), heart)
    case (_, suit) => (new Lover(face, resources + (suit -> card)), resources get suit)
  }

  override def formationBonus(resource : Suit) = this match {
    case Formation(Spectre) =>
      (face.rank, resource) match {
        case (King | Queen, Diamond) => 1
        case (King, Club) => 3
        case (Queen, Club) => 2
        case (King, Spade) => 1
      }
    case _ => super.formationBonus(resource)
  }


}


object Formation {
  def unapply(being: Being) : Option[Formation] = being match {
    case Being(_, Some(_), None, None, None) => Some(Child)
    case Being(_, Some(_), Some(_), Some(_), Some(_)) => Some(Accomplished)
    case Being(_, Some(_), Some(_), None, Some(_)) => Some(Fool)
    case Being(_, None, Some(_), Some(_), Some(_)) => Some(Spectre)
    case Being(_, Some(_), None, Some(_), Some(_)) => Some(Wizard)
    case Being(_, Some(_), Some(_), Some(_), None) => Some(Knight)
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

