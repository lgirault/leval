package gp.leval.core

/** Created by Loïc Girault on 01/07/16.
  */
object Target {

  def apply(game: Game, c: Card): Seq[Target] = c match {
    case Card(_, s) => apply(game, s)
    case Joker(Joker.Black) =>
      (apply(game, Suit.Spade).toSet ++ apply(game, Suit.Club)).toSeq
    case Joker(Joker.Red) =>
      (apply(game, Suit.Heart).toSet ++ apply(game, Suit.Diamond)).toSeq
  }
  def apply(game: Game, playedSuit: Suit): Seq[Target] = playedSuit match {
    case Suit.Heart => Seq(SelfStar)
    case Suit.Club =>
      val riverAvailable =
        game.beingsOwnBy(game.currentStarIdx) exists {
          case Formation(Spectre) => true
          case _                  => false
        }

      val tgts = Source +: (for {
        s <- Suit.list
      } yield TargetBeingResource(s, game.stars.indices))

      if riverAvailable then DeathRiver +: tgts
      else tgts

    case Suit.Spade => // hard coded for two Players
      val opponentStarAvailable = {
        val opponentId = (game.currentStarIdx + 1) % 2
        game.beingsOwnBy(opponentId).forall(_.heart.isEmpty)
      }
      val tgts = Seq(TargetBeingResource(Suit.Heart, game.stars.indices))
      if opponentStarAvailable then OpponentStar +: tgts
      else tgts

    case Suit.Diamond =>
      val opponentId = (game.currentStarIdx + 1) % 2
      val opponentStar = game.stars(opponentId)
      val opponentHasSpectre = {
        game.beingsOwnBy(opponentId) exists {
          case Formation(Spectre) => true
          case _                  => false
        }
      }
      if opponentHasSpectre then Seq(OpponentSpectrePower)
      else Seq(OpponentStar, TargetBeingResource(Suit.Club, Seq(opponentId)))
  }
}
sealed abstract class Target
case object SelfStar extends Target
case object OpponentStar extends Target
case class TargetBeingResource(suit: Suit, sides: Seq[Int]) extends Target
case object OpponentSpectrePower extends Target

sealed abstract class CollectTarget extends Target
case object Source extends CollectTarget
case object DeathRiver extends CollectTarget
