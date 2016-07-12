package leval

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActorRef
import leval.core.{C, Diamond, Game, Heart, InfluencePhase, King, Move, Numeric, PlayerId, Spade}
import leval.gui.CardImg
import leval.gui.gameScreen.{BurialDialog, BurialPane, GameScreenControl, ObservableGame}
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

  override def receive: Receive = {
    case InfluencePhase(1) => leval.ignore(oGame(InfluencePhase(0)))
    case m : Move[_] => leval.ignore(oGame(m))
  }
}

object TestScene extends JFXApp  {

  val (twilight, g) = Game.twilight(Game(PlayerId(69, "Betelgeuse"), PlayerId(42, "AlphaCentauri")))

  val game = new ObservableGame(g)

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

  control showTwilight twilight
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

  new BurialDialog(
    Seq(C(King, Spade),
      C(Numeric(5), Heart),
      C(Numeric(7), Diamond)),
    CardImg.width, CardImg.height,
    fp).showAndWait()
}