package leval.gui.gameScreen

//import cats._
import leval.core.{Being, Card, Game, Move, MutableGame, Phase, Star, Suit}

import scala.collection.mutable

/**
  * Created by Loïc Girault on 01/07/16.
  */

trait GameObserver {
  def notify[A](m : Move[A], res : A) : Unit
}

class ObservableGame(g : Game) extends MutableGame(g){
  def stars : Seq[Star] = game.stars
  def currentStarId : Int = game.currentStarId
  def currentPhase : Phase = game.currentPhase
  def deathRiver: List[Card] = game.deathRiver
  def currentRound : Int = game.currentRound
  def currentStar : Star = game.currentStar
  def ended : Boolean = game.ended
  def result = game.result
  def findBeing(face : Card) = game.findBeing(face)

  def nextPhase = game.nextPhase

  def beingsState = game.beingsState
  def lookedCards = game.lookedCards
  def revealedCard = game.revealedCard

  def rules = game.rules

  def value(b : Being, s : Suit ) = g.beingValue(b, s)

  val observers : mutable.ListBuffer[GameObserver] = new mutable.ListBuffer[GameObserver]

  def notifyAll[A](ma: Move[A], res : A) : Unit =
    observers.foreach(_.notify(ma,res))

  override def apply[A](ma: Move[A]): A /*Id[A]*/ = {
    val res = super.apply(ma)
    notifyAll(ma, res)
    res
  }



}
