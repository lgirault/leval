package leval.gui.gameScreen

import cats._
import leval.core.{Being, Card, Deck, FaceCard, Game, Move, MutableGame, RoundState, Star, Suit}

import scala.collection.mutable

/**
  * Created by Loïc Girault on 01/07/16.
  */

trait GameObserver {
  def notify[A](m : Move[A], res : A) : Unit
}

class ObservableGame(g : Game) extends MutableGame(g){

  def stars : Seq[Star] = game.stars
  def currentPlayer : Int = game.currentPlayer
  def roundState : RoundState = game.roundState
  def beingsState : Map[FaceCard, Being.State] = game.beingsState
  def lookedCards : Set[(FaceCard, Suit)] = game.lookedCards
  def source : Deck = game.source
  def deathRiver: Seq[Card] = game.deathRiver

  val observers : mutable.ListBuffer[GameObserver] = new mutable.ListBuffer[GameObserver]

  def notifyAll[A](ma: Move[A], res : A) : Unit =
    observers.foreach(_.notify(ma,res))

  override def apply[A](ma: Move[A]): Id[A] = {
    val res = super.apply(ma)
    notifyAll(ma, res)
    res
  }


}
