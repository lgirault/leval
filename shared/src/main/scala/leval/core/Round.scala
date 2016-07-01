package leval.core

/**
  * Created by lorilan on 6/25/16.
  */
sealed abstract class RoundState
case object InfluencePhase extends RoundState
case class ActPhase(activatedBeings : Set[FaceCard]) extends RoundState
case object SourcePhase extends RoundState

sealed abstract class RoundOwner
case object Self extends RoundOwner
case object Opponent extends RoundOwner

sealed abstract class Move[A]

//heart or diamond
case class DirectEffect( effect : (Int, Suit)) extends Move[Unit]
//diamond or spades
case class AttackBeing
( attack : (Int, Suit),
  target : FaceCard
) extends Move[Boolean]


//because club has several effect of different intensity
// its effects are modeled as a combination of smaller moves
//case class PowerEffect
//( collectFromRiver  : Boolean,
//  lookedCard : (FaceCard, Suit),
//  emitter : Option[FaceCard] //none if played from hand
//) extends Move[Unit]

case class RemoveFromHand(card : Card) extends Move[Unit]
case class ActivateBeing(card : FaceCard) extends Move[Unit]
case object Collect extends Move[Unit]
case object CollectFromRiver extends Move[Unit]
case class LookCard ( target : (FaceCard, Suit) ) extends Move[Unit]

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