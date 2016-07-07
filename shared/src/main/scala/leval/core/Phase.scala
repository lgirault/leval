package leval.core

/**
  * Created by lorilan on 6/25/16.
  */
sealed abstract class Phase
case object InfluencePhase extends Phase
case class ActPhase(activatedBeings : Set[FaceCard]) extends Phase
case object SourcePhase extends Phase
