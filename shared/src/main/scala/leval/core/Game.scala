package leval.core

/**
  * Created by lorilan on 6/29/16.
  */
import Game.{SeqOps, goesToRiver}
case class Game
( stars : Seq[Star], // for 4 or 3 players ??
  currentPlayer : Int,
  roundState: RoundState,
  beingsState : Map[FaceCard, Being.State], //reset each round
  source : Deck,
  deathRiver: Seq[Card],
  values : Card => Int) {

  def nextPlayer = (currentPlayer + 1) % stars.length
  def currentStar : Star = stars(currentPlayer)
  //  def setCurrentStar(newStar : Star) : Game =
  //    copy(stars.set(currentPlayer, newStar))
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

  def directEffect(card : (Int, Suit)) : Game = {
    val (targetId, newTarget) =
      card match {
        case (value, Heart) =>
          (currentPlayer,
            currentStar.copy(majesty = currentStar.majesty + value))
        case (value, Diamond | Spade) =>

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

  def collect : Game = {
    val newHand = currentHand + source.head
    setCurrentHand(newHand).copy(source = source.tail)
  }

  def collectFromRiver : Game = {
    val newHand = currentHand + deathRiver.head
    setCurrentHand(newHand)
      .copy(deathRiver = deathRiver.tail)
  }

  def placeBeing(being : Being) : Game = {
    val newHand = currentHand -- being.cards
    val g = setCurrentHand(newHand)
    g.setCurrentPlayerBeing(being)
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



  def attackBeing(attack : (Int, Suit),
                  target : FaceCard ) : (Game, Boolean) = {
    val (targetB, owner) = findBeing(target)
    val (amplitude, suit) = attack

    val (heartCasualty, powerCasualty) = beingsState getOrElse   (target, (0, 0))

    val (cardRemoved, newState) = suit match {
      case Spade =>
        val hp  = targetB.value(Heart, values).get
        val dmgs = heartCasualty + amplitude
        val s = beingsState + (target -> ((dmgs, powerCasualty)))
        (dmgs >= hp, s)

      case Diamond =>
        val hp  = targetB.value(Club, values).get
        val dmgs = powerCasualty + amplitude
        val s = beingsState + (target -> ((heartCasualty, dmgs)))
        (dmgs >= hp, s)

      case _ => leval.error()
    }
    val newStars =
      if(cardRemoved) {
        val removedSuit : Suit = suit match {
          case Spade => Heart case Diamond => Club
          case _ => leval.error()
        }
        val newBeing = targetB.copy(resources = targetB.resources - removedSuit)
        val ownerStar = stars(owner)
        stars.set(owner, ownerStar.copy(beings = ownerStar.beings + (target  -> newBeing)))
      } else stars

    (copy(stars = newStars, beingsState = newState), cardRemoved)

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

  def endPhase : Game = roundState match {
    case InfluencePhase => copy(roundState = ActPhase(Set()))
    case ActPhase(_) => copy(roundState = SourcePhase)
    case SourcePhase =>
      copy(currentPlayer = nextPlayer,
        roundState = InfluencePhase,
        beingsState = Map())
  }
}

import cats.{Id, ~>}

object Game {

  def apply(s1 : Star, s2 : Star, src : Deck) =
    new Game(Seq(s1, s2), 0, InfluencePhase, Map(), src, Seq(), Card.value)

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


  def apply(g: Game) : Move ~> Id =
    new (Move ~> Id) {
      var game = g
      def apply[A](ma : Move[A]) : Id[A] = ma match {
        case DirectEffect(card) => game = game.directEffect(card)
        case AttackBeing(attack, target) =>
          val (g, killed) = game.attackBeing(attack, target)
          game = g
          killed
        case RemoveFromHand(card) => game = game.removeFromHand(card)
        case ActivateBeing(card) => game = game.activateBeing(card)
        case Collect => game = game.collect
        case CollectFromRiver => game = game.collectFromRiver
        case LookCard(_) => ()
        case PlaceBeing(being) => game = game.placeBeing(being)
        case RemoveBeing(card) => game = game.removeBeing(card)
        case PlaceCardsToRiver(cards) => game = game.placeCardsToRiver(cards)
        case Educate(cards, target) => game = game.educate(cards, target)
        case EndPhase => game = game.endPhase
      }
    }

}
