package leval

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActorRef
import leval.core._
import leval.gui.gameScreen.{BurialDialog, CardImg, GameScreenControl, ObservableGame}
import leval.gui.text.Fr

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.layout.FlowPane

/**
  * Created by lorilan on 7/4/16.
  */


object ControllerMockup {
  def props(oGame : ObservableGame) = Props(new ControllerMockup(oGame))
}
class ControllerMockup
( val oGame : ObservableGame
) extends Actor {

  val playerId = 0
  val opponentId = 1

  override def receive: Receive = {
    case InfluencePhase(opponentId) => leval.ignore(oGame(InfluencePhase(playerId)))
    case m @ AttackBeing(_,target,_) =>
      val (toBury, _) = oGame(m)
      val b = oGame.game.beings(target.face)
      if(b.owner == opponentId && (
        b match {
          case Formation(_) => false
          case _ => true //dead being
        }))
        oGame(Bury(b.face, toBury.toList))
    case m : Move[_] =>
      //println("Controller Mockup receives" + m)
      leval.ignore(oGame(m))

  }
}

object TestScene extends JFXApp  {

  implicit def numPair2card(p:(Int, Suit)) : C = C(0, Numeric(p._1), p._2)
  implicit def facePair2card(p:(Face, Suit)) : C = C(0, p._1, p._2)

  def initGame : Game = {


    val child = new Being(1,
      (Jack, Spade),
      Map(Heart -> ((2, Heart)))
    )
    val wizard = new Being(1,
      (Queen, Heart),
      Map(Club -> ((8, Club)),
        Heart -> ((King, Heart)),
        //Spade -> ((1, Spade)),
        Diamond -> ((1, Diamond))
      ))
    val spectre = new Being(0,
      (King, Spade),
      Map(Club -> ((1, Club)),
        Diamond -> ((6, Diamond)),
        Spade -> ((Jack, Spade))
      ))

    import Joker._
    val inHandForTest : Seq[Card] =
      Seq(J(0, Red), J(0, Black),
        (3, Spade), (2, Heart), (2, Club), (2, Diamond))

    val (p1, p2) = (PlayerId(69, "Betelgeuse"), PlayerId(42, "AlphaCentauri"))

    val usedCards : Set[Card] = wizard.cards.toSet ++ spectre.cards ++ inHandForTest

    val deck = core.deck54() filterNot usedCards.contains

    val (d2, hand1) = deck.pick(9)
    val (d3, hand2) = d2.pick(9)

    Game(Star(p1, hand1 ++ inHandForTest),
      Star(p2, hand2), d3, Antares).
      copy(beings =
        Map(wizard.face -> wizard,
          spectre.face -> spectre,
          child.face -> child))
  }



  val game = new ObservableGame(initGame)

  implicit val system = ActorSystem()
  //  val _ = system.actorOf(ControlerMockup.props(game))



  val stageScene =  new Scene(800, 600)

  stage = new JFXApp.PrimaryStage {
    title = "Test"

    implicit val txt = Fr
    scene = stageScene

  }

  val control = new GameScreenControl(stageScene, game, 0, TestActorRef(ControllerMockup.props(game)))

  control.notify(InfluencePhase(game.currentStarId), ())
  // control showTwilight twilight
}
import TestScene.{numPair2card, facePair2card}

object BurialTestScene extends JFXApp {
  val fp = new FlowPane

  stage = new JFXApp.PrimaryStage {
    title = "Test"
    implicit val txt = Fr
    scene = new Scene {
      root = fp
    }
  }

  val b = Being(0,
    (King, Spade),
    Map[Suit, Card](Heart -> ((5, Heart)),
      Diamond -> ((7, Diamond))))
  new BurialDialog(BuryRequest(b, b.cards.toSet),
    CardImg.width, CardImg.height,
    fp).showAndWait()
}