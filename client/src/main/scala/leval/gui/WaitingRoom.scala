package leval
package gui

import core.PlayerId
import leval.network.client._
import leval.network.protocol.GameDescription

import scala.collection.mutable.ListBuffer
import scalafx.Includes._
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, VBox}


abstract class WaitingRoom
( network : NetWorkController,
  partyName : String,
  maxPlayer : Int) extends BorderPane
  with WaitingOtherPlayerView {

  def gameScreen(desc : GameDescription) : Unit /* BattleMapController*/

  val label = new Label(s"$partyName - Waiting for players ...")

  val players = ListBuffer[PlayerId]()
  val playersLabel = new VBox()

//  val plopper =  new Button("Plopper"){
//    handleEvent(MouseEvent.MouseClicked) {
//      () => println("plop")
//        val _ = playersLabel.children += new Label("- Plop !!")
//    }
//  }

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

  def gameReady(launcher : GameLauncher, usr : UserMapRelationship) : Unit = {
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

/*
 val label = new Label(s"$partyName - Waiting for players ...")
  val players = new VBox()

  val plopper =  new Button("Plopper"){
    handleEvent(MouseEvent.MouseClicked) {
      () => println("plop")
       val _ = players.children += new Label("- Plop !!")
    }
  }

  center = new FlowPane(){
    children = List(label, plopper, players)
  }


  //val players = ListBuffer[PlayerId]()



  def addPlayer : PlayerId => Unit = {
    case pid @ PlayerId(_, name) =>
      //players += pid
    //  updateCurrentNumPlayer(players.size)
      players.children += new Label("- " + pid.name)

  }
 */