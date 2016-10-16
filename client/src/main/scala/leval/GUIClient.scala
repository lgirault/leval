package leval

import java.io.{BufferedReader, InputStreamReader}
import java.net._

import com.typesafe.config.Config
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import leval.gui.SearchingServerScene
import leval.network.{IdentifyingActor, Settings}
import java.util

import akka.event.Logging
import leval.utils.ConnectionHelper

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object GUIClient extends JFXApp {

  val stageScene =  new Scene{
    root = new SearchingServerScene()
  }

  val (widthRatio, heightRatio)  = (16d, 9d)

  stage = new PrimaryStage {
    title = "Le Val des Étoiles"
    scene = stageScene
    minHeight = 800
    minWidth = 600
  }

  val (systemName, actorName) =
    ("ClientSystem", "IdentifyingActor")

  val conf = ConnectionHelper.conf("client")

  val server = conf getString "leval.server.hostname"
  val serverPort = conf getString "leval.server.port"

  val system = ActorSystem(systemName, conf)

  val log = Logging.getLogger(system, this)

  log info s"hostname = ${conf getString "akka.remote.netty.tcp.hostname"}"
  log info s"port = ${conf getString "akka.remote.netty.tcp.port"}"
  log info s"bind-hostname = ${conf getString "akka.remote.netty.tcp.bind-hostname"}"
  log info s"bind-port = ${conf getString "akka.remote.netty.tcp.bind-port"}"

  log info s"server = $server"
  log info s"serverPort = $serverPort"


  override def stopApp(): Unit = System exit 0


  val serverPath = Settings.remotePath(server, serverPort)
  log info s"trying to connect to $serverPath"

  system.actorOf(IdentifyingActor.props(serverPath, stageScene), actorName)

}
