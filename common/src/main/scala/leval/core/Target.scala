package leval.core

/**
  * Created by Loïc Girault on 01/07/16.
  */
object Target {

  def apply(game : Game, c : Card) : Seq[Target] = c match {
    case Card(_, s) => apply(game, s)
    case Joker(Joker.Black) =>  (apply(game, Spade).toSet ++ apply(game, Club)).toSeq
    case Joker(Joker.Red) =>  (apply(game, Heart).toSet ++ apply(game, Diamond)).toSeq
  }
  def apply(game : Game, playedSuit : Suit) : Seq[Target] = playedSuit match {
    case Heart => Seq(SelfStar)
    case Club =>
      val riverAvailable =
        game beingsOwnBy game.currentStarIdx exists {
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
        val opponentId = (game.currentStarIdx + 1) % 2
        game beingsOwnBy opponentId forall (_.heart.isEmpty)
      }
      val tgts = Seq(TargetBeingResource(Heart, game.stars.indices))
      if(opponentStarAvailable)
        OpponentStar +: tgts
      else tgts

    case Diamond =>
      val opponentId = (game.currentStarIdx + 1) % 2
      val opponentStar = game.stars(opponentId)
      val opponentHasSpectre = {
        game beingsOwnBy opponentId exists {
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

sealed abstract class CollectTarget extends Target
case object Source extends CollectTarget
case object DeathRiver extends CollectTarget

