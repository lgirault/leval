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
case class DirectEffect
( card : Card,
  emitter : Option[FaceCard] //none if played from hand
) extends Move[Game]

case class AttackBeing
( card : Card,
  target : FaceCard,
  emitter : Option[FaceCard] //none if played from hand
) extends Move[Game]

case object Collect extends Move[Game]
case object CollectFromRiver extends Move[Game]
case class LookCard
( targetBeing  : FaceCard,
  targetResource : Suit
) extends Move[Game]
case class PlaceBeing(being: Being) extends Move[Game]
case class Educate(cards : Seq[Card]) extends Move[Game]

