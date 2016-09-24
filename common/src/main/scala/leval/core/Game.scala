package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
import Game.{SeqOps, StarIdx, goesToRiver}

object Game {
  type StarIdx = Int
  implicit class SeqOps[T](val s : Seq[T]) extends AnyVal {
    def set(idx : Int, newVal : T) : Seq[T] = {
      val (s0, _ +: s1) = s.splitAt(idx)
      s0 ++: (newVal +: s1)
    }

    def set(idx : Int, f : T => T) : Seq[T] = {
      val (s0, sidx +: s1) = s.splitAt(idx)
      s0 ++: (f(sidx) +: s1)
    }
  }

  //  def activateOrDiscard()
  def goesToRiver(card : Card) : Boolean = card match {
    case Card((Jack | Queen | King), Diamond) => false
    case _ => true
  }
}

case class Game
(rules : CoreRules,
 stars : Seq[Star], // for 4 or 3 players ??
 source : Deck,
 currentStarIdx : StarIdx = 0,
 currentPhase: Phase = InfluencePhase(0),
 beings : Map[Card, Being] = Map(),
 deathRiver: List[Card] = List(),
 currentRound : Int = 1,
 beingsState : Map[Card, Being.State] = Map(), //reset each round
 lookedCards : Set[(Card, Suit)] = Set(),
 revealedCard : Set[(Card, Suit)] = Set()
) {


  def nextPlayer = (currentStarIdx + 1) % stars.length
  def currentStar = stars(currentStarIdx)

  def ended = rules.ended(this)

  def result = rules.result(this)


  def nextPhase : Phase = currentPhase match {
    case InfluencePhase(_)=> ActPhase(Set())
    case ActPhase(_) => SourcePhase
    case SourcePhase => InfluencePhase(nextPlayer)
  }

  //def currentStar : Star = stars(currentPlayer)
  def setStar(numStar : Int, f : Star => Star) : Game =
  copy(stars = stars.set(numStar, f))

  def +(b : Being) =
    copy(beings = beings + (b.face -> b))



  def directEffect(value : Int, target : Int) : Game = {
    val s = stars(target)
    copy(stars = stars.set(target,  s.copy(majesty = s.majesty + value)))
  }

  def activateBeing(face : Card) : Game = currentPhase match {
    case ActPhase(activated) => copy(currentPhase = ActPhase(activated + face))
    case _ => this
  }

  def removeFromHand(card : Card) : Game = {
    val newStars  = stars map (_ - card)

    if(goesToRiver(card)) copy(stars = newStars, deathRiver = card :: deathRiver)
    else copy(stars = newStars)
  }

  def collect(origin: Origin, target: CollectTarget) : (Game, Card) = {
    val (g1, c) = target match {
      case Source => (copy(source = source.tail), source.head)
      case DeathRiver => (copy(deathRiver = deathRiver.tail), deathRiver.head)
    }
    val g2 = origin match {
      case CardOrigin.Being(b, _) if !b.hasAlreadyDrawn =>
        g1 + b.copy(hasAlreadyDrawn = true)
      case _ => g1
    }
    (g2.setStar(origin.owner, _ + c), c)
  }


  def placeBeing(being : Being, side : StarIdx) : (Game, Option[Card]) = {
    val g1 = setStar(side, _ -- being.cards)
      .copy(beings = beings + (being.face -> being))
    being.face match {
      case Card(King, _) | Card(Jack, Heart)  =>
        beings.find {
          case (Card(Queen, _), b @ Spectre(BlackLady)) =>
            b.lovedOne contains being.face
          //TODO check if 4 players game requires lover to be same card
          //or just same card value
          case _ => false
        } map {
          case (c, b) => (g1.directEffect(15, b.owner), Some(c))
        } getOrElse ((g1, None))
      case _ => (g1, None)
    }


  }


  def burry(target : Card, cards : List[Card])  : Game =
    copy(beings = beings - target,
      deathRiver = cards reverse_::: deathRiver)


  def attackBeing(origin : CardOrigin,
                  target : Being,
                  targetedSuit : Suit) : (Game, Set[Card], Int) = {


    val (g0, removed0)  = rules.onAttack(this, origin, target).
      revealCard(target.face, targetedSuit, Some(origin))

    val (g1, removed1) =
      if(removed0) (g0, removed0)
      else {
        val amplitude: Int = origin match {
          case CardOrigin.Hand(_, c) => rules.value(c)
          case CardOrigin.Being(b, s) => beingValue(b, s).get
        }
        import Being.StateOps
        val targetState = beingsState getOrElse(target.face, (0, 0))


        val hp = beingValue(target, targetedSuit).get
        val targetNewState = targetState.add(targetedSuit, amplitude)

        val globalNewState = beingsState + (target.face -> targetNewState)
        val removeCard = (targetNewState get targetedSuit) >= hp


        ((if (removeCard)
          rules.removeArcanumFromBeing(g0, Some(origin), target, targetedSuit)
        else g0).copy(beingsState = globalNewState), removeCard)
      }

    val g2 = origin match {
      case CardOrigin.Hand(_, c) =>
        g1.removeFromHand(c)
      case CardOrigin.Being(_, _) => g1
    }

    val toDraw =
      if(removed1 && targetedSuit == Club)
        rules.wizardOrEminenceGrise(origin)
      else 0

    val (g3, toBury) =
      if(removed1) target - targetedSuit match {
        case Formation(_) => (g2, Set[Card]())
        case b => rules.onDeath(g2, origin, target, targetedSuit)
      }
      else (g2, Set[Card]())
    println(toDraw)
    (g3, toBury, toDraw)
  }




  def educate(e : Educate) : Game = {
    val b = beings(e.target)

    val star = stars(b.owner)

    def aux(b : Being, s : Star) : (Star, Being) =
      e match {
        case Switch(target, c) =>
          val (newB, oldC) = b.educateWith(c)
          (s.copy(hand = s.hand - c + oldC), newB)
        case Rise(target, cards) =>
          (s.copy(hand = s.hand -- cards.values), b.educateWith(e))
      }


    val (newStar, newBeing) = aux(b, star)

    copy(beings = beings + (newBeing.face -> newBeing),
      stars = stars.set(b.owner, newStar))
  }

  def revealAndLookLoverCheck(targetfc : Card,
                              s : Suit,
                              sAttacker : Option[CardOrigin],
                              removeGard : Int => Boolean) : (Game, Boolean) = {
    val targetB = beings(targetfc)
    val looked = targetB resources s
    looked match {
      case Card(Queen | King, _) if removeGard(targetB.owner)  =>
        (rules.removeArcanumFromBeing(this, sAttacker, targetB, Heart), true)
      case _ => (this, false)
    }
  }

  def lookCard (o : CardOrigin, targetfc : Card, s : Suit) : (Game, Boolean) = {
    val(g, cardRemoved) =
      revealAndLookLoverCheck(targetfc, s, None, _ != o.owner)
    (g.copy(lookedCards = lookedCards + ((targetfc, s))),
      cardRemoved)
  }

  def revealCard (targetfc : Card, s : Suit, sAttacker : Option[CardOrigin]) : (Game, Boolean) = {
    val(g, cardRemoved) =  revealAndLookLoverCheck(targetfc, s, sAttacker, _ => true)
    (g.copy(revealedCard = revealedCard + ((targetfc, s))),
      cardRemoved)
  }


  def beginPhase(newPhase : Phase) : Game = newPhase match {
    case InfluencePhase(newActivePlayer) =>
      copy(currentPhase = newPhase,
        currentStarIdx = newActivePlayer,
        currentRound = currentRound + 1)
    case ActPhase(_) =>
      copy(currentPhase = newPhase)
    case SourcePhase =>
      copy(currentPhase = SourcePhase,
        beingsState = Map(),
        lookedCards = Set(),
        revealedCard = Set())
  }

  def beingValue(b : Being, s : Suit ) = b.value(s, rules.value)
  def arcaneBonus(b : Being, s : Suit ) = b.bonus(s, rules.value)
  def beingsOwnBy(idx: StarIdx) = beings.values filter (_.owner == idx)

}


//import cats.{Id, ~>}
class MutableGame(var game : Game) /*extends (Move ~> Id)*/ {

  def source = game.source
  def stars : Seq[Star] = game.stars
  def currentStarId : Int = game.currentStarIdx
  def currentPhase : Phase = game.currentPhase
  def deathRiver: List[Card] = game.deathRiver
  def currentRound : Int = game.currentRound
  def currentStar : Star = game.currentStar
  def ended : Boolean = game.ended
  def result = game.result
  def beings = game.beings
  def beingsOwnBy(idx: StarIdx) = game.beingsOwnBy(idx)

  def nextPhase = game.nextPhase

  def beingsState = game.beingsState
  def lookedCards = game.lookedCards
  def revealedCard = game.revealedCard

  def rules = game.rules

  def value(b : Being, s : Suit) : Option[Int] = game.beingValue(b, s)
  def arcaneBonus(b : Being, s : Suit ) = game.arcaneBonus(b, s)


  def apply[A](ma: Move[A]): A /*Id[A]*/ = ma match {
    case MajestyEffect(v, ps) => game = game.directEffect(v, ps)
    case AttackBeing(attack, target, targetedSuit) =>
      val (g, toBury, numToCollect) = game.attackBeing(attack, target, targetedSuit)
      game = g
      (toBury, numToCollect)
    case RemoveFromHand(card) => game = game.removeFromHand(card)
    case ActivateBeing(card) => game = game.activateBeing(card)
    case Collect(o, t) =>
      val (g, collected) = game.collect(o, t)
      game = g
      collected
    case LookCard(origin, target, resource) =>
      val (g, cardRemoved) = game.lookCard(origin, target, resource)
      game = g
      cardRemoved
    case Reveal(target, resource) =>
      val (g, cardRemoved) = game.revealCard(target, resource, None)
      game = g
      cardRemoved

    case PlaceBeing(being, side) =>
      val (g, darkLadyRemoved) = game.placeBeing(being, side)
      game = g
      darkLadyRemoved
    case Bury(target, cards) => game = game.burry(target, cards)
    case e : Educate => game = game.educate(e)
    case p : Phase => game = game.beginPhase(p)
  }


}
