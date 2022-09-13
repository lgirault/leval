package gp.leval.core

import gp.leval
import java.util.Date
import cats.implicits.*
import cats.effect.kernel.{Sync, Clock}
import cats.effect.std.Random

/** Created by lorilan on 9/2/16.
  */
case class GameInit(
    seed: Long,
    twilight: Twilight,
    stars: List[Star], // for 4 or 3 players ??
    source: Deck,
    rules: Rules
) extends Serializable {

  def game: Game = {
    val g = Game(rules.coreRules, stars, source)
    if rules.ostein then
      g.copy(currentStarIdx = -1, currentPhase = InfluencePhase(-1))
    else g

  }

  def doTwilight: GameInit = {
    // tant qu'on a pas deux cartes égales, on continue de piocher (le val 1, p 20)
    val (d, List(h1, h2)) = GameInit.doTwilight(source)

    val List(s01, s02) = stars
    val (s1, s2) = (s01 ++ h1, s02 ++ h2)

    if Card.value(h1.head) > Card.value(h2.head) then
      copy(twilight = Twilight(List(h1, h2)), stars = List(s1, s2), source = d)
    else copy(twilight = Twilight(List(h2, h1)), stars = List(s2, s1), source = d)
  }
}

object GameInit:

  def apply[F[_]](players: List[PlayerId], rule: Rules)(using F: Sync[F]): F[GameInit] = players match {
    case p1 :: p2 :: Nil => this.apply(p1, p2, rule)
    case _               => F.raiseError(new Exception("two players only"))
  }

  def apply[F[_]](pid1: PlayerId, pid2: PlayerId, rules: Rules)
  (using F: Sync[F], clock: Clock[F]): F[GameInit] = 
    import rules.{coreRules as crules}
    for {
      now <- clock.realTime
      seed = now.toMillis
      r <- Random.scalaUtilRandomSeedLong(seed)
      deck <- r.shuffleList(deck54())

      // on pioche 9 carte
      (d2, hand1) = deck.pick(9)
      (d3, hand2) = d2.pick(9)

    } yield   new GameInit(
      seed,
      Twilight(List()),
      List(
        Star(pid1, crules.startingMajesty, hand1),
        Star(pid2, crules.startingMajesty, hand2)
      ),
      d3,
      rules
    )
  

  def hasFace(h: Set[Card]) =
    h.exists {
      case Joker(_)                     => true
      case Card(Rank.King | Rank.Queen | Rank.Jack, _) => true
      case _                            => false
    }
  def mulligan(g: Game): Boolean =
    g.stars.exists(s => !hasFace(s.hand))

  def gameWithoutMulligan[F[_]](players: List[PlayerId], rules: Rules)(using F: Sync[F]): F[GameInit] = {
    GameInit(players, rules).flatMap{ gi0 =>
      val gi = gi0.doTwilight
      if mulligan(gi.game) then gameWithoutMulligan(players, rules)
      else F.pure(gi)

    }
  }

  def doTwilight(source: List[Card]): (List[Card], List[List[Card]]) = {
    var d = source
    var h1 = List(d.head)
    d = d.tail
    var h2 = List(d.head)
    d = d.tail

    while Card.value(h1.head) == Card.value(h2.head) do
      d match {
        case c1 :: c2 :: remainings =>
          d = remainings
          h1 = c1 :: h1
          h2 = c2 :: h2

        case Nil | List(_) => leval.error()
      }
    (d, List(h1, h2))
  }
