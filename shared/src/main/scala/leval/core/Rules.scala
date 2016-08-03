package leval.core

/**
  * Created by lorilan on 6/21/16.
  */

import Game.SeqOps

trait Rules {

  def value(c : Card) : Int // Variante de sinnlos. As = 11

  def startingMajesty : Int

  val losingMajesty : Int = 0
  val winningMajesty : Int = 100

  val maxPlayer : Int = 2

  //way to specific
  //we could generalize that with a (situation, being)=> effect kind of function
  //situation = onDraw/onDeath/onKill/onAttack // ??
  //effect = majestyEffect (target = Origin Star, Targeted Star), draw, retrieve killed face,
  //but then how to handle contextual effect like fool *first* collect of helios ?


  //def onCollect(g : Game, b : Being) : (Game, Option[PlayerInput]) = (g, None)
  def wizardCollect : Int
  def foolFirstCollect : Int

  def drawAndLookValues(origin : Origin) : (Int, Int) =
    origin match {
      case CardOrigin.Hand(_, C(King, _)) => (1, 3)
      case CardOrigin.Hand(_, C(Queen, _)) => (1, 2)
      case CardOrigin.Hand(_, _) => (1, 1)
      case co @ CardOrigin.Being(b, _) =>
        (b, co.card) match {
          case (Formation(Fool), C(Jack, _)) => (3, 3)
          case (Formation(Fool), _) => (2, 1)
          case (Formation(Wizard), C(_, Diamond)) => (wizardCollect, 0) // draw on kill
          case (_, C(Jack, _)) => (2, 2)
          case _ => (1, 1)
        }
      case Origin.Star(_) => (1, 0)
    }

  def isButcher(o : CardOrigin) : Boolean =
    (o, o.card) match {
      case (CardOrigin.Being(_, _), C(Jack, Spade)) => true
      case _ => false
    }

  // return (game, being after attack)
  def onAttack
  (g : Game,
   attacker : CardOrigin,
   attacked : Being
  ) : Game = //attack self -5 points
  if(attacker.owner == attacked.owner){
    val malus = if(attacked.lover) 10
    else 5
    g.copy(stars = g.stars.set(attacked.owner, _ - malus))
  }
  else g


  def collect(g: Game, origin: Origin, target: CollectTarget) : (Game,Seq[Card]) = {
      val (g1, c) = g.collect(target)
    (origin, target) match {
      case (CardOrigin.Being(Spectre(BlackLady), _), DeathRiver) =>
        val (g2, c2) = g1.collect(DeathRiver)
        (g2.setStar(origin.owner, _ + c + c2), Seq(c, c2))
      case _ => (g1.setStar(origin.owner, _ + c), Seq(c))
    }
  }

  def removeArcanumFromBeing
  (g : Game,
   sAttacker : Option[CardOrigin], //no attacker on reveal
   attacked : Being,
   targetedSuit : Suit) : Game = {
    val removedArcana = attacked resources targetedSuit
    val newBeing = attacked - targetedSuit

    val g1 = newBeing match {
      case Formation(Spectre) => g.setStar(attacked.owner, _ - 5) + newBeing
      case _ => g + newBeing
    }

    (Game.goesToRiver(removedArcana), sAttacker exists isButcher) match {
        case (false, _) => g1
        case (_, false) => g1.copy(deathRiver = removedArcana :: g.deathRiver)
        case (true, true) =>
          removedArcana match {
            case c @ (C( King | Queen | Jack , _) | Joker(_)) =>
              g1.setStar(sAttacker.get.owner, _ + removedArcana)
            case _ =>
              g1.copy(deathRiver = removedArcana :: g.deathRiver)
          }
      }
  }


  // return (game, card to burry, number of card the killer can draw)
  def onDeath
  (g : Game,
   killer : CardOrigin,
   killed : Being
  ) : (Game, Set[Card], Int) = {
    val g2 = childAndDauphinEffect(g, killer, killed)
    val g3 = spectreEffectOnDeath(g2, killed)
    val (g4, toBurry) = butcherEffect(g3, killer, killed)
    (g4, toBurry, wizardOrEminenceGrise(killer))
  }


  def butcherEffect
  (g : Game,
   killer : CardOrigin,
   killed : Being ) : (Game, Set[Card]) =
  if(isButcher(killer)){
    val f : Card => Boolean = {
      case c @ (C( King | Queen | Jack , _) | Joker(_)) =>
        Game.goesToRiver(c)
      case _ => false
    }
    val (kept, toBury) = killed.cards.toSet partition f
    (g.setStar(killer.owner, _ ++ kept), toBury)
  }
  else (g, killed.cards.toSet)


  def wizardOrEminenceGrise(killer : CardOrigin) : Int =
    (killer, killer.card) match {
      case (CardOrigin.Being(Formation(Wizard), _), C(Jack, _)) => 2
      case (CardOrigin.Being(Formation(Wizard), _), _)
           | (CardOrigin.Being(_, Diamond), C(Jack, _)) => 1
      case _ => 0
    }

  def spectreEffectOnDeath( g : Game, killed : Being ) : Game = {
    killed match {
      case Formation(Spectre) => g.setStar(killed.owner, _ + 5)
      case _ => g
    }
  }

  def childAndDauphinEffect
  (g : Game,
   killer : CardOrigin,
   killed : Being
  ) : Game = {
    val childMalus = killed match {
      case Formation(Child) => 5
      case _ => 0
    }
    val dauphinMalus = killed.resources get Heart match {
      case Some(C(Jack, Heart)) => 5
      case _ => 0
    }
    g.setStar(killer.owner, _  - (childMalus + dauphinMalus))
  }



  def shadowAllowed : Boolean

  //default = 2 players
  //winner, loser
  def result(g : Game) : Option[(PlayerId, PlayerId)] =
  if(g.source.isEmpty) None
  else {
    val someWinner = g.stars.zipWithIndex.find {
      case (s, i) => s.majesty == winningMajesty
    }
    val result = someWinner map {
      case (s, i) => (s.id, g.stars(i+1%2).id)
    }
    if(result.nonEmpty) result
    else {
      val someLoser = g.stars.zipWithIndex.find {
        case (s, i) => s.majesty == losingMajesty
      }
      someLoser map {
        case (s, i) => (g.stars(i+1%2).id, s.id)
      }
    }
  }


  def ended(g : Game) : Boolean =
    g.source.isEmpty || g.stars.exists(s =>
      s.majesty == losingMajesty ||
        s.majesty == winningMajesty)


  def canSoulBeSold : Boolean //variante du diable
  //variante Janus à 4 joueur
  //variante Nédémone, narrative
  //variante O'Stein, draft en début de partie

  //utiliser un décorateur pour implanter les variantes ?


  def legalLoverFormationAtCreation(c : Formation) : Boolean

  def otherLover = PartialFunction[Rank, Rank] {
    case King => Queen
    case Queen => King
  }

  def checkLegalLover(face : Card, heart : Card) =
    (face, heart) match {
      case (C(fr @ (King | Queen), fs), C(hr, hs)) =>
        hr == otherLover(fr) && fs == hs
      case _ => false
    }

  def validResource(face : Card, c : Card, pos : Suit) : Boolean

  def validResources(b : Being) : Boolean =  b.resources forall {
    case (Heart, c @ C(_, _)) if b.lover => checkLegalLover(b.face, c)
    case (pos, c) => validResource(b.face, c, pos)
  }

  def validBeing(b: Being): Boolean = b match {
    case Formation(Shadow) if shadowAllowed => validResources(b)
    case Formation(_) => validResources(b)
    case _ => false
  }


}

trait SinnlosAntaresCommon {
  self : Rules =>

  def value(c: Card): Int = Card.value(c)

  val startingMajesty : Int = 25

  val wizardCollect : Int = 1

  val foolFirstCollect : Int = 2

  val canSoulBeSold : Boolean = false

  val shadowAllowed : Boolean = false


}

object Sinnlos
  extends Rules
    with SinnlosAntaresCommon
    with Serializable {

  override val toString = "Sinnlos"
  def legalLoverFormationAtCreation(c : Formation) : Boolean =
    c == Accomplished

  def validResource(face : Card, c : Card, pos : Suit) = c match {
    case C(Numeric(_), `pos`) => true
    case _ => false
  }

}

trait AntaresHeliosCommon {
  def legalLoverFormationAtCreation(c : Formation) : Boolean = true

  def validResource(face : Card, c : Card, pos : Suit) = (c, face) match {
    case (C(Numeric(_), `pos`) | Joker(_), _)
       | (C(Jack, `pos`), C(King|Queen, `pos`)) => true
    case _ => false
  }
}

object Antares
  extends Rules
    with SinnlosAntaresCommon
    with AntaresHeliosCommon
    with Serializable{
  override val toString = "Antarès"
}

object Helios
  extends Rules
    with AntaresHeliosCommon
    with Serializable {

  override val toString = "Hélios"

  def value(c: Card): Int = Card.value(c)

  val startingMajesty : Int = 36

  val wizardCollect : Int = 2

  val foolFirstCollect : Int = 3

  val canSoulBeSold : Boolean = false

  val shadowAllowed : Boolean = true

}