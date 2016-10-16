package leval.control

import akka.actor.ActorRef
import com.typesafe.config.Config
import leval.gui.text.ShowRules
import leval.network.{JoinAction, _}
import leval.LevalConfig.ConfigOps

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{HBox, VBox}
import scalafxml.core.macros.sfxml

/**
  * Created by lorilan on 9/30/16.
  */
@sfxml
class GameListControl
( private val actor : ActorRef,
  private val config : Config,
  private val vBox : VBox)
  extends ListingListener {

  def refresh(): Unit = {
    vBox.children.clear()
    actor ! ListGame
  }

  implicit val lang = config.lang()

  def handleDescription
  (gameDescription: GameDescription,
   currentNumPlayer: Int,
   answer : JoinAction) : Unit = {
    import gameDescription._
    val hbox = new HBox()
    hbox.children += new Label(s"${owner.name} - ${ShowRules(rules)} ($currentNumPlayer/${rules.maxPlayer})")
    val node : Node = new Button("Join"){
      handleEvent(MouseEvent.MouseClicked)(answer)
    }
    hbox.children += node

    val _ = vBox.children += hbox
  }

  def startScreen() : Unit = {
    actor ! StartScreen
  }
}
