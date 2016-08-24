package leval

import java.io.{BufferedReader, InputStreamReader}
import java.net.{InetAddress, NetworkInterface, URL}

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import leval.gui.SearchingServerScene
import leval.network.Settings
import leval.network.client.{IdentifyingActor, NetWorkController}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene



object GUIClient extends JFXApp {

  def findIP() : Unit = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    while(interfaces.hasMoreElements) {
      val interface : NetworkInterface = interfaces.nextElement()
      val addresses = interface.getInetAddresses
      while (addresses.hasMoreElements) {
        val address : InetAddress = addresses.nextElement
        println(address.getHostAddress)
      }
    }
  }

  val server = System getProperty "leval.server.hostname"
  val serverPort = System getProperty "leval.server.port"

  println(s"server = $server")
  println(s"serverPort = $serverPort")

  findIP()
  println("private ip = " + InetAddress.getLocalHost.getHostAddress)

  //public ip
  val whatismyip = new URL("http://checkip.amazonaws.com")
  val in = new BufferedReader(new InputStreamReader(whatismyip.openStream()))
  val ip = in.readLine() //you get the IP as a String
  println("public ip = " + ip)

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
