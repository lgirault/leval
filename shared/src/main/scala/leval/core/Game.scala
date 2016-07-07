package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
import Game.{SeqOps, goesToRiver}
case class Game
(stars : Seq[Star], // for 4 or 3 players ??
 currentPlayer : Int,
 roundState: Phase,
 source : Deck,
 values : Card => Int,
 deathRiver: Seq[Card] = Seq(),
 //three below should be move into ActPhase
 beingsState : Map[FaceCard, Being.State] = Map(), //reset each round
 lookedCards : Set[(FaceCard, Suit)] = Set(),
 revealedCard : Set[(FaceCard, Suit)] = Set()
) {

  def nextPlayer = (currentPlayer + 1) % stars.length
  def currentStar : Star = stars(currentPlayer)
  def setCurrentStar(newStar : Star) : Game =
    copy(stars.set(currentPlayer, newStar))
  def setHand(numStar : Int, newHand : Set[Card]) : Game = {
    val star = stars(numStar)
    copy(stars.set(numStar, star.copy(hand = newHand)))
  }


  def currentHand : Set[Card] = currentStar.hand
  def setCurrentHand(newHand : Set[Card]) : Game = setHand(currentPlayer, newHand)
  def setCurrentPlayerBeing(being : Being) : Game = {
    val newPlayer = currentStar.copy(beings = currentStar.beings + (being.face -> being))
    copy(stars = stars.set(currentPlayer, newPlayer))
  }

  def directEffect(value : Int, playedSuit : Suit) : Game = {
    val (targetId, newTarget) =
      playedSuit match {
        case Heart =>
          (currentPlayer,
            currentStar.copy(majesty = currentStar.majesty + value))
        case Diamond | Spade =>
          val target = stars(nextPlayer)
          (nextPlayer,
            target.copy(majesty = target.majesty - value))
        case _ => leval.error()
      }

    copy(stars = stars.set(targetId, newTarget))

  }

  def activateBeing(face : FaceCard) : Game = {
    val ActPhase(activated) = roundState
    copy(roundState = ActPhase(activated + face))
  }

  def removeFromHand(card : Card) : Game = {
    val newHand = currentHand - card
    val g = setCurrentHand(newHand)
    if(goesToRiver(card)) g.copy(deathRiver = deathRiver :+ card )
    else g
  }

  def collect : (Game, Card) = {
    val newHand = currentHand + source.head
    (setCurrentHand(newHand).copy(source = source.tail),
      source.head)
  }

  def collectFromRiver : (Game, Card)= {
    val newHand = currentHand + deathRiver.head
    (setCurrentHand(newHand)
      .copy(deathRiver = deathRiver.tail),
      deathRiver.head)
  }

  def placeBeing(being : Being) : Game = {
    val newHand = currentHand -- being.cards
    val g = setCurrentHand(newHand)
    val g2 = g.setCurrentPlayerBeing(being)
    being match {
      case Formation(Spectre) =>
        g2.setCurrentStar(g2.currentStar.copy(majesty =
          g2.currentStar.majesty - 5 ))
      case _ => g2
    }
  }

  def removeBeing(target : FaceCard) : Game = {
    val (targetB, owner) = findBeing(target)
    val ownerStar = stars(owner)
    val newStars = stars.set(owner, ownerStar.copy(beings = ownerStar.beings - target))
    copy(stars = newStars)
  }

  def placeCardsToRiver(cards : Seq[Card])  : Game =
    copy(deathRiver = deathRiver ++ cards)

  def findBeing(face : FaceCard) : (Being, Int) = {
    var i = nextPlayer
    do{
      val sb = stars(i).beings get face
      if(sb.nonEmpty)
        return (sb.get, i)

      i = (i+1) % stars.length
    }while(i != nextPlayer)
    leval.error()
  }



  def attackBeing(amplitude : Int,
                  target : FaceCard,
                  targetedSuit : Suit) : (Game, Boolean) = {
    val (targetB, owner) = findBeing(target)
    import Being.StateOps
    val targetState = beingsState getOrElse (target, (0, 0))

    val hp  = targetB.value(targetedSuit, values).get
    val targetNewState = targetState.add(targetedSuit, amplitude)

    val globalNewState = beingsState + (target -> targetNewState)
    val cardRemoved =(targetNewState get targetedSuit) >= hp

    val newStars =
      if(cardRemoved)
        removeCardFromBeing(stars, targetB, owner, targetedSuit)
      else stars

    (copy(stars = newStars, beingsState = globalNewState), cardRemoved)

  }

  private def removeCardFromBeing(stars : Seq[Star],
                                  target : Being, owner : Int,
                                  removedSuit : Suit) : Seq[Star] = {

    val newBeing = target.copy(resources = target.resources - removedSuit)
    val ownerStar = stars(owner)
    stars.set(owner, ownerStar.copy(beings = ownerStar.beings + (target.face  -> newBeing)))
  }

  def educate(cards : Seq[Card],
              target : FaceCard) : Game = {

    val b = currentStar.beings(target)
    /*getOrElse stars(nextPlayer).being(target).get*/
    // in antares there is a mirror's power that allow to educated once on the opposite side

    val (oldCards, newB) =
      cards.foldLeft((List[Option[Card]](), b)){
        case ((acc, b), c) =>
          val (b2, sc) = b educateWith c
          (sc :: acc, b2)
      }

    val g2 =
      if(oldCards.flatten.nonEmpty){
        val List(retrievedCard) = oldCards.flatten //substitution allow only one card
        setCurrentHand(currentHand -- cards + retrievedCard)
      } else setCurrentHand(currentHand -- cards)

    setCurrentPlayerBeing(b)
  }

  def revealAndLookLoverCheck(targetfc : FaceCard, s : Suit) : (Seq[Star], Boolean) = {
    val (targetB, owner) = findBeing(targetfc)
    val looked = targetB resources s
    looked match {
      case (Queen | King, _)  if owner != currentPlayer  =>
        (removeCardFromBeing(stars, targetB, owner, Heart), true)
      case _ => (stars, false)
    }
  }
  def lookCard (targetfc : FaceCard, s : Suit) : (Game, Boolean) = {
    val(newStars, cardRemoved) = revealAndLookLoverCheck(targetfc, s)
    (copy(stars = newStars,
      lookedCards = lookedCards + ((targetfc, s))), cardRemoved)
  }

  def revealCard (targetfc : FaceCard, s : Suit) : (Game, Boolean) = {
    val(newStars, cardRemoved) = revealAndLookLoverCheck(targetfc, s)
    (copy(stars = newStars,
      revealedCard = revealedCard + ((targetfc, s))), cardRemoved)
  }


  def endPhase : Game = roundState match {
    case InfluencePhase => copy(roundState = ActPhase(Set()))
    case ActPhase(_) => copy(roundState = SourcePhase)
    case SourcePhase =>
      copy(currentPlayer = nextPlayer,
        roundState = InfluencePhase,
        beingsState = Map(),
        lookedCards = Set(),
        revealedCard = Set())
  }
}

import cats.{Id, ~>}

object Game {

  def apply(s1 : Star, s2 : Star, src : Deck) =
    new Game(Seq(s1, s2), 0, InfluencePhase, src, Card.value)

  def apply(players : Seq[PlayerId]) : Game = players match {
    case p1 +: p2 +: Nil => this.apply(p1, p2)
    case _ => leval.error("two players only")
  }
  def apply(pid1 : PlayerId, pid2 : PlayerId) : Game = {
    val deck = deck54()

    // on pioche 9 carte + 1 qui sert à déterminer qui commence
    val (d2, hand1) = deck.pick(10)
    val (d3, hand2) = d2.pick(10)

    //tant qu'on a pas deux carte différente, on continue de piocher (le val 1, p 20)
    var h1 = hand1
    var h2 = hand2
    var d = d3
    while(Card.value(h1.head) == Card.value(h2.head)) d match {
      case c1 +: c2 +: remainings =>
        d = remainings
        h1 = c1 +: h1
        h2 = c2 +: h2
      case Nil | Seq(_)=> ???
    }

    val (s1, s2) =
      if(Card.value(h1.head) > Card.value(h2.head))
        (Star(pid1, h1), Star(pid2, h2))
      else
        (Star(pid2, h2), Star(pid1, h1))

    Game(s1, s2, d)

  }


  implicit class SeqOps[T](val s : Seq[T]) extends AnyVal {
    def set(idx : Int, newVal : T) : Seq[T] = {
      val (s0, _ +: s1) = s.splitAt(idx)
      s0 ++: (newVal +: s1)
    }
  }

  //  def activateOrDiscard()

  def goesToRiver(card : Card) : Boolean = card match {
    case ((Jack | Queen | King), Diamond) => false
    case _ => true
  }

}

class MutableGame(var game : Game) extends (Move ~> Id) {

  def apply[A](ma: Move[A]): Id[A] = ma match {
    case MajestyEffect(v, ps) => game = game.directEffect(v, ps)
    case AttackBeing(attack, target, targetedSuit) =>
      val (g, cardRemoved) = game.attackBeing(attack, target, targetedSuit)
      game = g
      cardRemoved
    case RemoveFromHand(card) => game = game.removeFromHand(card)
    case ActivateBeing(card) => game = game.activateBeing(card)
    case CollectFromSource =>
      val (g, collected) = game.collect
      game = g
      collected
    case CollectFromRiver =>
      val (g, collected) = game.collectFromRiver
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

    case PlaceBeing(being) => game = game.placeBeing(being)
    case RemoveBeing(card) => game = game.removeBeing(card)
    case PlaceCardsToRiver(cards) => game = game.placeCardsToRiver(cards)
    case Educate(cards, target) => game = game.educate(cards, target)
    case EndPhase => game = game.endPhase
  }


}
