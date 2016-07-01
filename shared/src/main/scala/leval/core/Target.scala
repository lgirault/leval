package leval.core

/**
  * Created by Loïc Girault on 01/07/16.
  */
object Target {
  def apply(game : Game, playedSuit : Suit) : Seq[Target] = playedSuit match {
    case Heart => Seq(SelfStar)
    case Club =>
      val riverAvailable =
        game.currentStar.beings.values exists {
          case Formation(Spectre) => true
          case _ => false
        }

      val tgts = Source +: (for {
        s <- Suit.list
      } yield TargetBeingResource(s, game.stars.indices))

      if(riverAvailable) DeathRiver +: tgts
      else tgts

    case Spade => //hard coded for two Players
      val opponentStarAvailable = {
        val opponentStar = game.stars((game.currentPlayer + 1) % 2)
        opponentStar.beings.values forall  (_.heart.isEmpty )
      }
      val tgts = Seq(TargetBeingResource(Heart, game.stars.indices))
      if(opponentStarAvailable)
        OpponentStar +: tgts
      else tgts

    case Diamond =>
      val opponentId = (game.currentPlayer + 1) % 2
      val opponentStar = game.stars(opponentId)
      val opponentHasSpectre = {
        opponentStar.beings.values exists {
          case Formation(Spectre) => true
          case _ => false
        }
      }
      if(opponentHasSpectre) Seq(OpponentSpectrePower)
      else Seq(OpponentStar, TargetBeingResource(Club, Seq(opponentId)))
  }
}
sealed abstract class Target
case object SelfStar extends Target
case object OpponentStar extends Target
case class TargetBeingResource(suit : Suit, sides : Seq[Int]) extends Target
case object OpponentSpectrePower extends Target
case object Source extends Target
case object DeathRiver extends Target
