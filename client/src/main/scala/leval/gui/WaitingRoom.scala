package leval
package gui

import leval.core.{PlayerId, Rules}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.gui.text.ValText
import leval.network.client._

import scala.collection.mutable.ListBuffer
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, VBox}


class WaitingRoom
(control : NetWorkController,
 partyName : String,
 rules : Rules)
(implicit txt : ValText) extends BorderPane {
  pane =>


  def gameScreen(game : ObservableGame) : GameScreenControl ={
    val pidx = game.stars.indexWhere(_.id == control.thisPlayer)
    val gcontrol =
      new GameScreenControl(game, pidx, control.actor)

    control.scene.root = gcontrol.pane
    gcontrol
  }
  val label = new Label(s"$partyName - $rules - Waiting for players ...")

  val players = ListBuffer[PlayerId]()
  val playersLabel = new VBox()

  center = new FlowPane(){
    children = List(label, playersLabel)
  }

  bottom = new Button("Back"){
    handleEvent(MouseEvent.MouseClicked) {
      () => control.displayStartScreen()
    }
  }

  def addPlayer(pid : PlayerId) : Unit = {
     println(s"addPlayer($pid)")
     val PlayerId(_, name) = pid
      players += pid
      updateCurrentNumPlayer(players.size)
      val _ = playersLabel.children += new Label("- " + name)

  }
  def clearPlayers() : Unit = {
    players.clear()
    playersLabel.children.clear()
  }

  private def updateCurrentNumPlayer(n : Int) =
    label.text = s"$partyName - $rules - Waiting for players ... $n / ${rules.maxPlayer}"

  def ownerExitAlert() =
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = txt.information
      headerText = txt.owner_exit
    }.showAndWait()

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