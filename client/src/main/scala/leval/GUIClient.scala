package leval

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import leval.gui.SearchingServerScene
import leval.network.Settings
import leval.network.client.{IdentifyingActor, NetWorkController}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene



object GUIClient extends JFXApp {

  val stageScene =  new Scene{
    root = new SearchingServerScene()
  }

  val (widthRatio, heightRatio)  = (16d,9d)
  //val (widthRatio, heightRatio)  = (4d,3d)

  stage = new PrimaryStage {
    title = "Le Val des Étoiles"
    scene = stageScene
    minHeight = 800
    minWidth = 600
  }

  val (systemName, actorName) =
    ("ClientSystem", "IdentifyingActor")

  val conf = ConfigFactory.load("client")
  val server = System getProperty "leval.server.hostname"
  val serverPort = System getProperty "leval.server.port"

  val system = ActorSystem(systemName, conf)

  val serverPath = Settings.remotePath(server, serverPort)
  println(s"connecting to $serverPath")

  def clientActor(netControl : NetWorkController) : ActorRef = {
    system.actorOf(IdentifyingActor.props(netControl, serverPath), actorName)
  }

  val control = new NetWorkController {
      val scene = stageScene
      clientActor(this)
  }

  override def stopApp() : Unit = {
    control.disconnect()
    println("Shutting down !!")
    system.terminate()
    println("Bye bye !!")
    System.exit(0)
  }
}
