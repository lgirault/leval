package leval.core

/**
  * Created by lorilan on 6/25/16.
  */
sealed abstract class RoundState
case object InfluencePhase extends RoundState
case object ActPhase extends RoundState
case object SourcePhase extends RoundState
case object OtherPlayerRound extends RoundState

sealed abstract class RoundOwner
case object Self extends RoundOwner
case object Opponent extends RoundOwner
