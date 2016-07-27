package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
import Game.{SeqOps, goesToRiver, StarIdx}

case class Game
(stars : Seq[Star], // for 4 or 3 players ??
 currentStarIdx : StarIdx,
 currentPhase: Phase,
 source : Deck,
 rules : Rules,
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
  def setHand(numStar : Int, newHand : Set[Card]) : Game = {
    val star = stars(numStar)
    copy(stars.set(numStar, star.copy(hand = newHand)))
  }


  def directEffect(value : Int, target : Int) : Game = {
    val s = stars(target)
    copy(stars = stars.set(target,  s.copy(majesty = s.majesty + value)))
  }

  def activateBeing(face : Card) : Game = {
    val ActPhase(activated) = currentPhase
    copy(currentPhase = ActPhase(activated + face))
  }

  def removeFromHand(card : Card) : Game = {
    val newStars  = stars.map {
      s =>
        if(s.hand contains card)
          s.copy(hand = s.hand - card)
        else s
    }
    if(goesToRiver(card)) copy(stars = newStars, deathRiver = card :: deathRiver)
    else copy(stars = newStars)
  }

  def collect(numStar : Int) : (Game, Card) = {
    val newHand = stars(numStar).hand + source.head
    (setHand(numStar, newHand).copy(source = source.tail),
      source.head)
  }

  def collectFromRiver(numStar : Int) : (Game, Card)= {
    val newHand = stars(numStar).hand + deathRiver.head
    (setHand(numStar, newHand)
      .copy(deathRiver = deathRiver.tail),
      deathRiver.head)
  }

  def placeBeing(being : Being, side : StarIdx) : Game = {
    val newHand = stars(side).hand  -- being.cards
    setHand(side, newHand)
      .copy(beings = beings + (being.face -> being))

  }

  def burry(target : Card, cards : List[Card])  : Game =
    copy(beings = beings - target,
      deathRiver = cards reverse_::: deathRiver)





  def majestyEffectOnAttackBeing
  ( target : Being,
    attacker : Int,
    removed : Boolean
    ) : Game = {
    val ownerId = target.owner
    //deadSpectreBonus
    target match {
      case Formation(Spectre) if removed =>
        val ownerStar = stars(ownerId)
        copy(stars = stars.set(ownerId,
          ownerStar.copy(majesty = ownerStar.majesty + 5)))
      case _ =>
        val childPenalty = target match {
          case Formation(Child) if removed => 5
          case _ => 0
        }
        val selfAttackPenalty =
          if(attacker == currentStarIdx){
            if(target.lover) 10
            else 5
          }
          else 0

        val attackerStar = stars(attacker)

        copy(stars = stars.set(attacker,
          attackerStar.copy(majesty = attackerStar.majesty +
            childPenalty +
            selfAttackPenalty)))

    }
  }

  def attackBeing(origin : Origin,
                  target : Card,
                  targetedSuit : Suit) : (Game, Boolean) = {
    val (g0, removed0)  = revealCard(target, targetedSuit)

    val (g1, removed1) =
      if(removed0) (g0, removed0)
      else {
        val amplitude: Int = origin match {
          case Origin.Hand(c) => rules.value(c)
          case Origin.BeingPane(b, s) => beingValue(b, s).get
        }
        val targetB = beings(target)
        import Being.StateOps
        val targetState = beingsState getOrElse(target, (0, 0))


        val hp = beingValue(targetB, targetedSuit).get
        val targetNewState = targetState.add(targetedSuit, amplitude)

        val globalNewState = beingsState + (target -> targetNewState)
        val removeCard = (targetNewState get targetedSuit) >= hp


        ((if (removeCard) removeCardFromBeing(targetB, targetedSuit)
        else g0).copy(beingsState = globalNewState), removeCard)
      }

    val g2 = origin match {
      case Origin.Hand(c) =>
        g1.copy(deathRiver = g1.deathRiver)
          .removeFromHand(c)
      case Origin.BeingPane(_, _) => g1
    }

    val b = beings(target)
    (g2.majestyEffectOnAttackBeing(b, origin.owner(this), removed1), removed1)
  }




  private def removeCardFromBeing(target : Being,
                                  removedSuit : Suit) : Game = {

    val removedCard = target resources removedSuit
    val newRiver =
      if(goesToRiver(removedCard)) removedCard :: deathRiver
      else deathRiver

    val newBeing = target.copy(resources = target.resources - removedSuit)
    copy( beings = beings + (target.face  -> newBeing),
      deathRiver = newRiver )
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
          (s.copy(hand = s.hand -- cards), b.educateWith(e))
      }


    val (newStar, newBeing) = aux(b, star)

    copy(beings = beings + (newBeing.face -> newBeing),
        stars = stars.set(b.owner, newStar))
  }

  def revealAndLookLoverCheck(targetfc : Card,
                              s : Suit,
                              removeGard : Int => Boolean) : (Game, Boolean) = {
    val targetB = beings(targetfc)
    val looked = targetB resources s
    looked match {
      case C(Queen | King, _) if removeGard(targetB.owner)  =>
        (removeCardFromBeing(targetB, Heart), true)
      case _ => (this, false)
    }
  }

  def lookCard (targetfc : Card, s : Suit) : (Game, Boolean) = {
    val(g, cardRemoved) =
      revealAndLookLoverCheck(targetfc, s, _ != currentStarIdx)
    (g.copy(lookedCards = lookedCards + ((targetfc, s))),
      cardRemoved)
  }

  def revealCard (targetfc : Card, s : Suit) : (Game, Boolean) = {
    val(g, cardRemoved) =
      revealAndLookLoverCheck(targetfc, s, _ => true)
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
  def beingsOwnBy(idx: StarIdx) = beings.values filter (_.owner == idx)
}



object Game {

  type StarIdx = Int

  def apply(s1 : Star, s2 : Star, src : Deck) =
    new Game(Seq(s1, s2), 0, InfluencePhase(0), src, Sinnlos)

  def apply(players : Seq[PlayerId]) : Game = players match {
    case p1 +: p2 +: Nil => this.apply(p1, p2)
    case _ => leval.error("two players only")
  }
  def apply(pid1 : PlayerId, pid2 : PlayerId) : Game = {
    val deck = deck54()

    // on pioche 9 carte
    val (d2, hand1) = deck.pick(9)
    val (d3, hand2) = d2.pick(9)

    Game(Star(pid1, hand1), Star(pid2, hand2), d3)
  }


  implicit class SeqOps[T](val s : Seq[T]) extends AnyVal {
    def set(idx : Int, newVal : T) : Seq[T] = {
      val (s0, _ +: s1) = s.splitAt(idx)
      s0 ++: (newVal +: s1)
    }
  }

  //  def activateOrDiscard()

  def goesToRiver(card : Card) : Boolean = card match {
    case C((Jack | Queen | King), Diamond) => false
    case _ => true
  }

  def twilight(g : Game) : (Twilight, Game) = {
    //tant qu'on a pas deux cartes égales, on continue de piocher (le val 1, p 20)


    val Seq(s01, s02) = g.stars
    var d = g.source
    var h1 = Seq(d.head)
    d = d.tail
    var h2 = Seq(d.head)
    d = d.tail

    while(Card.value(h1.head) == Card.value(h2.head)) d match {
      case c1 +: c2 +: remainings =>
        d = remainings
        h1 = c1 +: h1
        h2 = c2 +: h2

      case Nil | Seq(_)=> ???
    }
    val (s1, s2) = (s01.copy(hand = s01.hand ++ h1), s02.copy(hand = s02.hand ++ h2))

    if(Card.value(h1.head) > Card.value(h2.head))
      (Twilight(Seq(h1, h2)), g.copy(stars = Seq(s1, s2), source = d))
    else
      (Twilight(Seq(h2, h1)), g.copy(stars = Seq(s2, s1)))
  }

  def hasFace(h : Set[Card]) =
    h.exists {
      case Joker(_) => true
      case C(King|Queen|Jack, _) => true
      case _ => false
    }
  def mulligan(g : Game) : Boolean =
    g.stars.exists (s => ! hasFace(s.hand))


  def gameWithoutMulligan(players : Seq[PlayerId]) : (Twilight, Game) = {
    val (t, g) = twilight(Game(players))
    if(mulligan(g)) gameWithoutMulligan(players)
    else (t, g)
  }


}

//import cats.{Id, ~>}
class MutableGame(var game : Game) /*extends (Move ~> Id)*/ {

  def apply[A](ma: Move[A]): A /*Id[A]*/ = ma match {
    case MajestyEffect(v, ps) => game = game.directEffect(v, ps)
    case AttackBeing(attack, target, targetedSuit) =>
      val (g, cardRemoved) = game.attackBeing(attack, target, targetedSuit)
      game = g
      cardRemoved
    case RemoveFromHand(card) => game = game.removeFromHand(card)
    case ActivateBeing(card) => game = game.activateBeing(card)
    case CollectFromSource(numStar) =>
      val (g, collected) = game.collect(numStar)
      game = g
      collected
    case CollectFromRiver(numStar) =>
      val (g, collected) = game.collectFromRiver(numStar)
      game = g
      collected
    case LookCard(target, resource) =>
      val (g, cardRemoved) = game.lookCard(target, resource)
      game = g
      cardRemoved
    case Reveal(target, resource) =>
      val (g, cardRemoved) = game.revealCard(target, resource)
      game = g
      cardRemoved

    case PlaceBeing(being, side) => game = game.placeBeing(being, side)
    case Bury(target, cards) => game = game.burry(target, cards)
    case e : Educate => game = game.educate(e)
    case p : Phase => game = game.beginPhase(p)
  }


}
