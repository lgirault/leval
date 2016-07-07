package leval

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActorRef
import leval.core.{Game, Move, PlayerId}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}

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
    title = "V Box Example"

    val control =
      new GameScreenControl(game, 0, TestActorRef(ControllerMockup.props(game)))

    scene = new Scene {
      root = control.pane
    }


    println("creating scene")
  }


  println("creating stage")
}
