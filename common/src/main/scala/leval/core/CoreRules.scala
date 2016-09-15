package leval.core

/**
  * Created by lorilan on 6/21/16.
  */

import Game.{SeqOps, StarIdx}

case class Rules
( coreRules: CoreRules,
  ostein : Boolean = false,
  allowMulligan: Boolean = false,
  nedemone : Boolean = false,
  janus : Boolean = false) {

  val maxPlayer : Int =
    if(janus) 4
    else 2
}

trait CoreRules {

  val id : String

  def value(c : Card) : Int // Variante de sinnlos. As = 11

  def startingMajesty : Int

  val losingMajesty : Int = 0
  val winningMajesty : Int = 100

  //TODO : move into Rules ?

  //way to specific
  //we could generalize that with a (situation, being)=> effect kind of function
  //situation = onDraw/onDeath/onKill/onAttack // ??
  //effect = majestyEffect (target = Origin Star, Targeted Star), draw, retrieve killed face,
  //but then how to handle contextual effect like fool *first* collect of helios ?


  //def onCollect(g : Game, b : Being) : (Game, Option[PlayerInput]) = (g, None)
  def wizardCollect : Int
  def foolFirstCollect : Int


  def wizardOrEminenceGrise(origin : CardOrigin) : Int =
    (origin, origin.card) match {
      case (CardOrigin.Being(Formation(Wizard), _), Card(Jack, Diamond)) => wizardCollect + 1
      case (CardOrigin.Being(Formation(Wizard), _), _) => wizardCollect
      case (CardOrigin.Being(_, _), Card(Jack, Diamond)) => 1
      case _ => 0
    }

  def drawAndLookValues(origin : Origin) : (Int, Int) =
    origin match {
      case CardOrigin.Hand(_, Card(King, _)) => (1, 3)
      case CardOrigin.Hand(_, Card(Queen, _)) => (1, 2)
      case CardOrigin.Hand(_, _) => (1, 1)
      case co @ CardOrigin.Being(b, Club) =>
        (b, co.card) match {
          case (Formation(Fool), Card(Jack, _)) =>
            if(b.firstDraw) (foolFirstCollect + 1, 3)
            else (3, 3)
          case (Formation(Fool), _) =>
            if(b.firstDraw) (foolFirstCollect, 1)
            else (2, 1)
          case (_, Card(Jack, _)) => (2, 2)
          case _ => (1, 1)
        }
      case co @ CardOrigin.Being(b, Diamond) =>
      (wizardOrEminenceGrise(co), 0)
      case Origin.Star(_) => (1, 0)
    }

  def isButcher(o : CardOrigin) : Boolean =
    (o, o.card) match {
      case (CardOrigin.Being(_, _), Card(Jack, Spade)) => true
      case _ => false
    }

  // return (game, being after attack)
  def onAttack
  (g : Game,
   attacker : CardOrigin,
   attacked : Being
  ) : Game = //attack self -5 points
  if(attacker.owner == attacked.owner){
    val malus = if(attacked.inLove) 10
    else 5
    g.copy(stars = g.stars.set(attacked.owner, _ - malus))
  }
  else g


  //when the dark lady draw a card to the river, she collect two cards instead of one
  //but if she can draw an additional card thanks to an Eminence Grise,
  //even in the river she will draw only a third card
  def numCardDrawPerAction
  ( origin: Origin,
    target: CollectTarget,
    remainingDrawAction : Int) : Int = {
    (origin, target) match {
      case (CardOrigin.Being(Spectre(BlackLady), _), DeathRiver)
        if remainingDrawAction == 2 => 2
      case _ => 1
    }
  }

  def removeArcanumFromBeing
  (g : Game,
   sAttacker : Option[CardOrigin],
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
          case c @ (Card( King | Queen | Jack , _) | Joker(_)) =>
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
   killed : Being,
   targetedSuit : Suit
  ) : (Game, Set[Card]) = {
    val g2 = childAndDauphinEffect(g, killer, killed)
    val g3 = spectreEffectOnDeath(g2, killed)
    val (g4, toBurry) = butcherEffect(g3, killer, killed)
    val removedCard = killed.resources(targetedSuit)
    (g4, toBurry.filter(Game.goesToRiver) - removedCard)
  }


  def butcherEffect
  (g : Game,
   killer : CardOrigin,
   killed : Being ) : (Game, Set[Card]) =
    if(isButcher(killer)){
      val f : Card => Boolean = {
        case c @ (Card( King | Queen | Jack , _) | Joker(_)) =>
          Game.goesToRiver(c)
        case _ => false
      }
      val (kept, toBury) = killed.cards.toSet partition f
      println("river = " + g.deathRiver)
      println(s"butcher effect : kept = $kept, toBury = $toBury")
      (g.setStar(killer.owner, _ ++ kept), toBury)
    }
    else (g, killed.cards.toSet)




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
      case Some(Card(Jack, Heart)) => 5
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
      case (s, i) => s.majesty >= winningMajesty
    }
    val result = someWinner map {
      case (s, i) => (s.id, g.stars((i+1)%2).id)
    }
    if(result.nonEmpty) result
    else {
      val someLoser = g.stars.zipWithIndex.find {
        case (s, i) => s.majesty <= losingMajesty
      }
      someLoser map {
        case (s, i) => (g.stars((i+1)%2).id, s.id)
      }
    }
  }


  def ended(g : Game) : Boolean =
    g.source.isEmpty || g.stars.exists(s =>
      s.majesty <= losingMajesty ||
        s.majesty >= winningMajesty)


  def canSoulBeSold : Boolean //variante du diable
  //variante Janus à 4 joueur
  //variante Nédémone, narrative
  //variante O'Stein, draft en début de partie

  //utiliser un décorateur pour implanter les variantes ?


  def legalLoverFormationAtCreation(c : Formation) : Boolean

  def isValidBeingAtCreation(g : Game, b : Being, side : StarIdx) : Boolean =
    b match {
      case Formation(f) =>
        isValidBeing(b) && (!b.inLove ||
          legalLoverFormationAtCreation(f)) && {

          val playerBeings = g beingsOwnBy side

          val hasSameFormation = playerBeings exists {
            case Formation(`f`) => true
            case _ => false
          }

          !hasSameFormation
        }
      case _ => false
    }

  def otherLover : PartialFunction[Rank, Rank] = {
    case King => Queen
    case Queen => King
  }

  def checkLegalLover(face : Card, heart : Card) : Boolean =
    (face, heart) match {
      case (Card(fr @ (King | Queen), fs), Card(hr, hs)) =>
        hr == otherLover(fr) && fs == hs
      case _ => false
    }


  //during being creation, we may not have a face
  def validResource(sFace : Option[Card],
                    otherResources : Map[Suit, Card],
                    c : Card, pos : Suit) : Boolean =
    validResource(sFace getOrElse C(-1, Numeric(5), Heart),
      otherResources, c, pos)
  //restrictive default : using King or Queen could authorize lovers or "hommes lige"
  //using a Joker could forbid the other


  def validResource(face : Card,
                    otherResources : Map[Suit, Card],
                    c : Card, pos : Suit) : Boolean

  def validResources(b : Being) : Boolean =  b.resources forall {
    case (Heart, c : C ) if b.inLove => checkLegalLover(b.face, c)
    case (pos, c) => validResource(b.face, b.resources, c, pos)
  }

  def isValidBeing(b: Being): Boolean = b match {
    case Formation(Shadow) =>
      shadowAllowed && validResources(b)
    case Formation(f) => validResources(b)
    case _ => false
  }



}

trait SinnlosAntaresCommon {
  self : CoreRules =>

  def value(c: Card): Int = Card.value(c)

  val startingMajesty : Int = 25

  val wizardCollect : Int = 1

  val foolFirstCollect : Int = 2

  val canSoulBeSold : Boolean = false

  val shadowAllowed : Boolean = false


}

object Sinnlos
  extends CoreRules
    with SinnlosAntaresCommon
    with Serializable {

  val id = "sinnlos"
  override val toString = "Sinnlos"
  def legalLoverFormationAtCreation(c : Formation) : Boolean =
    c == Accomplished

  def validResource(face : Card,
                    otherResources : Map[Suit, Card],
                    c : Card, pos : Suit) = c match {
    case Card(Numeric(_), `pos`) => true
    case _ => false
  }

}

trait AntaresHeliosCommon extends CoreRules {

  def legalLoverFormationAtCreation(c : Formation) : Boolean = true

  override def checkLegalLover(face : Card, heart : Card) : Boolean =
    super.checkLegalLover(face, heart) || {
      (face, heart) match {
        case (Card(fr@(King | Queen), Heart), Card(Jack, Heart)) => true
        case _ => false
      }
    }

  def validResource(face : Card,
                    otherResources : Map[Suit, Card],
                    c : Card, pos : Suit) = (face, c) match {
    case (_, Card(Numeric(_), `pos`))
         | (Card(King|Queen, `pos`), Card(Jack, `pos`)) => true

    //Lover
    case (Card(lover@(King | Queen), fsuit), Card(r : Face, s))
      if pos == Heart =>
      fsuit == s && otherLover(lover) == r

    case (j1 @ Joker(_), j2 @ Joker(_)) => j1 == j2
    case (_, Joker(_)) =>
      ! otherResources.values.exists(r => r.isInstanceOf[J] && r != c)
    case _ => false
  }
}

object Antares
  extends AntaresHeliosCommon
    with SinnlosAntaresCommon
    with Serializable{
  val id = "antares"
  override val toString = "Antarès"
}

object Helios
  extends AntaresHeliosCommon
    with Serializable {
  val id = "helios"
  override val toString = "Hélios"

  def value(c: Card): Int = Card.value(c)

  val startingMajesty : Int = 36

  val wizardCollect : Int = 2

  val foolFirstCollect : Int = 3

  val canSoulBeSold : Boolean = false

  val shadowAllowed : Boolean = true

}