package leval

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActorRef
import leval.core.{Ace, C, Game, Heart, Jack, Move, PlayerId, Queen, Spade, Twilight}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.gui.text.Fr

import scalafx.application.JFXApp
import scalafx.scene.Scene

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
    case m : Move[_] => leval.ignore(oGame(m))
  }
}

object TestScene extends JFXApp  {

  val game =
    new ObservableGame(Game(PlayerId(69, "Betelgeuse"), PlayerId(42, "AlphaCentauri")))

  implicit val system = ActorSystem()
//  val _ = system.actorOf(ControlerMockup.props(game))


  stage = new JFXApp.PrimaryStage {
    title = "Test"

    val control =
      new GameScreenControl(game, 0, TestActorRef(ControllerMockup.props(game)))

    implicit val txt = Fr
    scene = new Scene {
      root = control.pane
      control.showTwilight(
        Twilight(Seq(Seq(C(Ace, Heart),C(Jack, Heart), C(Queen, Heart)),
          Seq(C(Ace, Spade), C(Jack, Spade),C(Queen, Spade)))))

    }

  }
}
