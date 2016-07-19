package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
import Game.{SeqOps, goesToRiver}

case class Game
(stars : Seq[Star], // for 4 or 3 players ??
 currentStarId : Int,
 currentPhase: Phase,
 source : Deck,
 rules : Rules,
 deathRiver: List[Card] = List(),
 currentRound : Int = 1,
 beingsState : Map[Card, Being.State] = Map(), //reset each round
 lookedCards : Set[(Card, Suit)] = Set(),
 revealedCard : Set[(Card, Suit)] = Set()
) {


  def nextPlayer = (currentStarId + 1) % stars.length
  def currentStar = stars(currentStarId)

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


  def setPlayerBeing(numStar : Int, being : Being) : Game = {
    val newPlayer = stars(numStar).copy(beings = stars(numStar).beings + (being.face -> being))
    copy(stars = stars.set(currentStarId, newPlayer))
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

  def placeBeing(being : Being, side : Int) : Game = {
    val newHand = stars(side).hand  -- being.cards
    setHand(side, newHand)
      .setPlayerBeing(side, being)
  }

  def burry(target : Card, cards : List[Card])  : Game = {
    val (targetB, owner) = findBeing(target)
    val ownerStar = stars(owner)
    val newStars = stars.set(owner, ownerStar.copy(beings = ownerStar.beings - target))
    copy(stars = newStars,
      deathRiver = cards reverse_::: deathRiver)
  }


  def findBeing(face : Card) : (Being, Int) = {
    var i = nextPlayer
    do{
      val sb = stars(i).beings get face
      if(sb.nonEmpty)
        return (sb.get, i)

      i = (i+1) % stars.length
    }while(i != nextPlayer)
    leval.error()
  }

  def majestyEffectOnAttackBeing
  ( target : Being,
    attacker : Int,
    ownerId : Int,
    removed : Boolean
    ) : Game = {

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
          if(attacker == currentStarId){
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
        val (targetB, owner) = findBeing(target)
        import Being.StateOps
        val targetState = beingsState getOrElse(target, (0, 0))


        val hp = beingValue(targetB, targetedSuit).get
        val targetNewState = targetState.add(targetedSuit, amplitude)

        val globalNewState = beingsState + (target -> targetNewState)
        val removeCard = (targetNewState get targetedSuit) >= hp


        ((if (removeCard) removeCardFromBeing(stars, targetB, owner, targetedSuit)
        else g0).copy(beingsState = globalNewState), removeCard)
      }

    val g2 = origin match {
      case Origin.Hand(c) =>
        g1.copy(deathRiver = g1.deathRiver)
          .removeFromHand(c)
      case Origin.BeingPane(_, _) => g1
    }

    val (b, ownerId) = findBeing(target)
    (g2.majestyEffectOnAttackBeing(b, origin.owner(this), ownerId, removed1), removed1)
  }




  private def removeCardFromBeing(stars : Seq[Star],
                                  target : Being, owner : Int,
                                  removedSuit : Suit) : Game = {

    val removedCard = target resources removedSuit
    val newRiver =
      if(goesToRiver(removedCard)) removedCard :: deathRiver
      else deathRiver


    val newBeing = target.copy(resources = target.resources - removedSuit)
    val ownerStar = stars(owner)
    copy(stars = stars.set(owner,
      ownerStar.copy(beings = ownerStar.beings + (target.face  -> newBeing))),
      deathRiver = newRiver )
  }

  def educate(e : Educate) : Game = {
    val (b, sid) = findBeing(e.target)

    val star = stars(sid)

    def aux(b : Being, s : Star) : Star =
      e match {
        case Switch(target, c) =>
          val oldC = b.resources(c.suit)
          s.copy(hand = s.hand - c + oldC,
            beings = s.beings + (target -> b.educateWith(c)))
        case Rise(target, cards) =>
          s.copy(hand = s.hand -- cards,
            beings = s.beings + (target -> b.educateWith(e)))
      }


    copy(stars = stars.set(sid, aux(b, star)))
  }

  def revealAndLookLoverCheck(targetfc : Card,
                              s : Suit,
                              removeGard : Int => Boolean) : (Game, Boolean) = {
    val (targetB, owner) = findBeing(targetfc)
    val looked = targetB resources s
    looked match {
      case C(Queen | King, _) if removeGard(owner)  =>
        (removeCardFromBeing(stars, targetB, owner, Heart), true)
      case _ => (this, false)
    }
  }

  def lookCard (targetfc : Card, s : Suit) : (Game, Boolean) = {
    val(g, cardRemoved) =
      revealAndLookLoverCheck(targetfc, s, _ != currentStarId)
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
        currentStarId = newActivePlayer,
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
}



object Game {

  def apply(s1 : Star, s2 : Star, src : Deck) =
    new Game(Seq(s1, s2), 0, InfluencePhase(0), src, SinnlosRules)

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
