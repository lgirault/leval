package leval.core

/**
  * Created by Loïc Girault on 06/07/16.
  */
sealed abstract class Move[A]
//heart or diamond
case class MajestyEffect(value : Int, targetStar : Int) extends Move[Unit]
//diamond or spades
case class AttackBeing
(attack : Int,
 target : Card,
 targetedSuit : Suit
) extends Move[Boolean]

case class RemoveFromHand(card : Card) extends Move[Unit]
case class ActivateBeing(card : Card) extends Move[Unit]
case class CollectFromSource(star : Int) extends Move[Card]
case class CollectFromRiver(star : Int) extends Move[Card]
//look card is a club effect and only the user see the card
//reveal a card is an after effect of using a being resource
//both player can see the card until the sourcePhase
case class Reveal(target : Card, resource : Suit) extends Move[Boolean]
case class LookCard(target : Card, resource : Suit) extends Move[Boolean]

case class PlaceBeing(being: Being, side : Int) extends Move[Unit]
case class RemoveBeing(card : Card) extends Move[Unit]
case class PlaceCardsToRiver(cards : Seq[Card]) extends Move[Unit]

case class Educate( cards : Seq[C],
                    target : Card) extends Move[Unit]

sealed abstract class Phase extends Move[Unit]
case class InfluencePhase(newPlayer : Int) extends Phase
case class ActPhase(activatedBeings : Set[Card]) extends Phase
case object SourcePhase extends Phase

case class Twilight(cards : Seq[Seq[Card]])

//import cats.free.Free
//import cats.free.Free.liftF
//object Move{
//  type FMove[A] = Free[Move, A]
//
//  def directEffect(effect : (Int, Suit)) : FMove[Unit] =
//    liftF[Move, Unit](DirectEffect(effect))
//
//  def attackBeing(attack : (Int, Suit), target : Card) : FMove[Unit] =
//    liftF[Move, Unit](AttackBeing(attack, target))
//
//  def collect : FMove[Unit] =
//    liftF[Move, Unit](Collect)
//
//  def collectFromRiver : FMove[Unit] =
//    liftF[Move, Unit](CollectFromRiver)
//
//  def lookCard( targetBeing  : Card, targetResource : Suit ) : FMove[Unit] =
//    liftF[Move, Unit](LookCard(targetBeing, targetResource))
//
//  def placeBeing(being: Being) : FMove[Unit] =
//    liftF[Move, Unit](PlaceBeing(being))
//
//  def educate(cards : Seq[Card])  : FMove[Unit] =
//    liftF[Move, Unit](Educate(cards))
//}
