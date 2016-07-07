package leval.core

/**
  * Created by Loïc Girault on 06/07/16.
  */
sealed abstract class Move[A]
//heart or diamond
case class MajestyEffect(value : Int, playedSuit : Suit) extends Move[Unit]
//diamond or spades
case class AttackBeing
(attack : Int,
 target : FaceCard,
 targetedSuit : Suit
) extends Move[Boolean]

case class RemoveFromHand(card : Card) extends Move[Unit]
case class ActivateBeing(card : FaceCard) extends Move[Unit]
case object CollectFromSource extends Move[Card]
case object CollectFromRiver extends Move[Card]
//look card is a club effect and only the user see the card
//reveal a card is an after effect of using a being resource
//both player can see the card until the sourcePhase
case class Reveal(target : FaceCard, resource : Suit) extends Move[Boolean]
case class LookCard ( target : FaceCard, resource : Suit) extends Move[Boolean]

case class PlaceBeing(being: Being) extends Move[Unit]
case class RemoveBeing(card : FaceCard) extends Move[Unit]
case class PlaceCardsToRiver(cards : Seq[Card]) extends Move[Unit]

case class Educate( cards : Seq[Card],
                    target : FaceCard ) extends Move[Unit]
case object EndPhase extends Move[Unit]

//import cats.free.Free
//import cats.free.Free.liftF
//object Move{
//  type FMove[A] = Free[Move, A]
//
//  def directEffect(effect : (Int, Suit)) : FMove[Unit] =
//    liftF[Move, Unit](DirectEffect(effect))
//
//  def attackBeing(attack : (Int, Suit), target : FaceCard) : FMove[Unit] =
//    liftF[Move, Unit](AttackBeing(attack, target))
//
//  def collect : FMove[Unit] =
//    liftF[Move, Unit](Collect)
//
//  def collectFromRiver : FMove[Unit] =
//    liftF[Move, Unit](CollectFromRiver)
//
//  def lookCard( targetBeing  : FaceCard, targetResource : Suit ) : FMove[Unit] =
//    liftF[Move, Unit](LookCard(targetBeing, targetResource))
//
//  def placeBeing(being: Being) : FMove[Unit] =
//    liftF[Move, Unit](PlaceBeing(being))
//
//  def educate(cards : Seq[Card])  : FMove[Unit] =
//    liftF[Move, Unit](Educate(cards))
//}
