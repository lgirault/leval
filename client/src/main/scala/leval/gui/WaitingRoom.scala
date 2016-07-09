package leval
package gui

import leval.core.PlayerId
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.network.client._

import scala.collection.mutable.ListBuffer
import scalafx.Includes._
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, VBox}


class WaitingRoom
(control : NetWorkController,
 partyName : String,
 maxPlayer : Int) extends BorderPane {

  def gameScreen(game : ObservableGame) : GameScreenControl ={
    val pidx = game.stars.indexWhere(_.id == control.thisPlayer)
    val gcontrol =
      new GameScreenControl(game, pidx, control.actor)

    control.scene.root = gcontrol.pane
    gcontrol
  }
  val label = new Label(s"$partyName - Waiting for players ...")

  val players = ListBuffer[PlayerId]()
  val playersLabel = new VBox()

  center = new FlowPane(){
    children = List(label, playersLabel)
  }

  def addPlayer(pid : PlayerId) : Unit = {
     val PlayerId(_, name) = pid
      players += pid
      updateCurrentNumPlayer(players.size)
      val _ = playersLabel.children += new Label("- " + name)

  }

  private def updateCurrentNumPlayer(n : Int) =
    label.text = s"$partyName - Waiting for players ... $n / $maxPlayer"

  def gameReady(launcher : NetWorkController, usr : UserMapRelationship) : Unit = {
    val newLine = usr match {
      case Joiner => new Label("Game Ready ! Waiting to start ...")
      case Owner => new Button("Start !"){
        handleEvent(MouseEvent.MouseClicked) {
           () => launcher.startGame()
        }
      }
    }
    val _ = playersLabel.children += newLine
  }

}