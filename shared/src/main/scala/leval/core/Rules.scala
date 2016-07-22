package leval.core

/**
  * Created by lorilan on 6/21/16.
  */
trait Rules {

  def value(c : Card) : Int // Variante de sinnlos. As = 11

  def startingMajesty : Int

  val losingMajesty : Int = 0
  val winningMajesty : Int = 100


  //way to specific
  //we could generalize that with a (situation, being)=> effect kind of function
  //situation = onDraw/onDeath/onKill/onAttack // ??
  //effect = majestyEffect (target = Origin Star, Targeted Star), draw, retrieve killed face,
  //but then how to handle contextual effect like fool *first* collect of helios ?
  def wizardCollect : Int
  def foolFirstCollect : Int
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

  def validResource(c : Card, pos : Suit) : Boolean

  def validResources(b : Being) : Boolean =  b.resources.toList forall {
    case (Heart, c @ C(_, _)) if b.lover => checkLegalLover(b.face, c)
    case (pos, c) => validResource(c, pos)
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

object SinnlosRules
  extends Rules
    with SinnlosAntaresCommon
    with Serializable {

  def legalLoverFormationAtCreation(c : Formation) : Boolean =
    c == Accomplished

  def validResource(c : Card, pos : Suit) = c match {
    case C(Numeric(_), `pos`) => true
    case _ => false
  }

}

trait AntaresHeliosCommon {
  def legalLoverFormationAtCreation(c : Formation) : Boolean = true

  def validResource(c : Card, pos : Suit) = c match {
    case C(Numeric(_) | Jack, `pos`)
         | Joker(_) => true
    case _ => false
  }
}

object Antares
  extends Rules
    with SinnlosAntaresCommon
    with AntaresHeliosCommon
    with Serializable

object Helios
  extends Rules
    with AntaresHeliosCommon
    with Serializable {
  def value(c: Card): Int = Card.value(c)

  val startingMajesty : Int = 36

  val wizardCollect : Int = 2

  val foolFirstCollect : Int = 3

  val canSoulBeSold : Boolean = false

  val shadowAllowed : Boolean = true

}