package leval.core

import scala.collection.immutable.SortedSet

/**
  * Created by Loïc Girault on 20/06/16.
  */
class Being
(val face : FaceCard,
 val heart : Option[Card],
 val weapon : Option[Card],
 val mind : Option[Card],
 val power : Option[Card]){

  def copy(face : FaceCard = face,
           heart : Option[Card] = heart,
           weapon : Option[Card] = weapon,
           mind : Option[Card] = mind,
           power : Option[Card] = power) : Being =
    new Being(face, heart, weapon, mind, power)

  //a being cannot be educated to become messianic or possessed so no ambiguity here
  def educateWith(card : Card) : (Being, Option[Card]) = card match {
    case (King | Queen , _) => (new Lover(face, Some(card), weapon, mind, power), heart)
    case (_, Heart) => (copy(heart = Some(card)), heart)
    case (_, Club) => (copy(power = Some(card)), power)
    case (_, Spade) => (copy(weapon = Some(card)), weapon)
    case (_, Diamond) => (copy(mind = Some(card)), mind)
  }

  def cards : Seq[Card] = List(Some(face), heart, weapon, mind, power).flatten
}

//separate class to contextualize the spectre and infer Dark Lady or Spectre Royal
class Lover
( face : FaceCard,
  heart : Option[Card],
  weapon : Option[Card],
  mind : Option[Card],
  power : Option[Card]) extends Being(face,heart, weapon, mind, power){

  override def copy(face : FaceCard = face,
                    heart : Option[Card] = heart,
                    weapon : Option[Card] = weapon,
                    mind : Option[Card] = mind,
                    power : Option[Card] = power) : Being =
    new Lover(face, heart, weapon, mind, power)

  override def educateWith(card : Card) : (Being, Option[Card]) = card match {
    case (_, Heart) => (new Being(face, Some(card), weapon, mind, power), heart)
    case _ => super.educateWith(card)
  }
}

sealed abstract class Formation
case object Accomplished
case object Fool
case object Knight
case object Child
case object Wizard
case object Spectre

object Being {
  type State = (Option[Int], Option[Int]) // (Heart casualty, Power Casualty)
}

object Star {
  def apply(id : PlayerId, hand : Seq[Card]) : Star = {
    val s : Set[Card] = SortedSet.empty(Card.cardOrdering)
    new Star(id, 25, s ++ hand, Map())
  }
}

case class Star
( id : PlayerId,
  majesty : Int,
  hand : Set[Card],
  beeings : Map[FaceCard, Being]) {

  def being(card : FaceCard) : Option[Being] = beeings get card
//    beeings.find(_.face == card)
}

