package leval
package gui

import leval.core.{PlayerId, Rules}
import leval.gui.text.{ShowRules, ValText}
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
(implicit texts : ValText) extends BorderPane {
  pane =>



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

  def rmPlayer(pid : PlayerId) : Unit = {
    players remove players.indexOf(pid)
    clearPlayers()
    players foreach addPlayer
  }

  def addPlayer(pid : PlayerId) : Unit = {
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
    label.text = s"$partyName - ${ShowRules(rules)} - Waiting for players ... $n / ${rules.maxPlayer}"

  def ownerExitAlert() =
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = texts.information
      headerText = texts.owner_exit
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