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
      leval.ignore(oGame(m))
      val b = oGame.game.beings(target.face)
      if(b.owner == opponentId && (
        b match {
        case Formation(_) => false
        case _ => true //dead being
      }))
        oGame(Bury(b.face, b.cards))
    case m : Move[_] =>
      println("Controller Mockup receives" + m)
      leval.ignore(oGame(m))

  }
}

object TestScene extends JFXApp  {

  def initGame : Game = {


    val child = new Being(1,
      C(Jack, Spade),
      Map(Heart -> ((2, Heart)))
    )
    val fool = new Being(1,
      C(Queen, Spade),
      Map(Club -> ((8, Club)),
        Heart -> ((1, Heart)),
        Spade -> ((1, Spade))
      ))
    val spectre = new Being(1,
      C(King, Spade),
      Map(Club -> ((1, Club)),
        Diamond -> ((6, Diamond)),
        Spade -> ((3, Spade))
      ))

    val inHandForTest : Seq[Card] = Seq(Joker.Red, Joker.Black, (3, Spade))

    val (p1, p2) = (PlayerId(69, "Betelgeuse"), PlayerId(42, "AlphaCentauri"))

    val usedCards : Set[Card] = fool.cards.toSet ++ spectre.cards ++ inHandForTest

    val deck = core.deck54() filterNot usedCards.contains

    val (d2, hand1) = deck.pick(9)
    val (d3, hand2) = d2.pick(9)

    Game(Star(p1, hand1 ++ inHandForTest),
      Star(p2, hand2), d3).
      copy(beings =
        Map(fool.face -> fool,
          spectre.face -> spectre,
          child.face -> child))
  }



  val game = new ObservableGame(initGame)

  implicit val system = ActorSystem()
  //  val _ = system.actorOf(ControlerMockup.props(game))
  val control = new GameScreenControl(game, 0, TestActorRef(ControllerMockup.props(game)))

  stage = new JFXApp.PrimaryStage {
    title = "Test"

    implicit val txt = Fr
    scene = new Scene {
      root = control.pane
    }
  }

 // control showTwilight twilight
}

object BurialTestScene extends JFXApp {
  val fp = new FlowPane

  stage = new JFXApp.PrimaryStage {
    title = "Test"
    implicit val txt = Fr
    scene = new Scene {
      root = fp
    }
  }

  new BurialDialog(Being(0,
      C(King, Spade),
      Map[Suit, Card](Heart ->C(Numeric(5), Heart),
      Diamond -> C(Numeric(7), Diamond))),
    CardImg.width, CardImg.height,
    fp).showAndWait()
}