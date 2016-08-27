package leval

import java.io.{BufferedReader, InputStreamReader}
import java.net._

import com.typesafe.config.Config
import akka.actor. ActorSystem
import com.typesafe.config.ConfigFactory
import leval.gui.SearchingServerScene
import leval.network.Settings
import leval.network.client.{IdentifyingActor, NetWorkController}
import java.util

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object ConnectionHelper {

  def findIP4(addresses : util.Enumeration[InetAddress]) : Option[String] =
    if(!addresses.hasMoreElements) None
    else {
      val address = addresses.nextElement()
      if(address.isInstanceOf[Inet4Address]) Some(address.getHostAddress)
      else findIP4(addresses)
    }

  def findLocalIP : Option[String] = {
    def aux(interfaces : util.Enumeration[NetworkInterface]) : Option[String] =
      if(!interfaces.hasMoreElements) None
      else {
        val interface : NetworkInterface = interfaces.nextElement()
        if(interface.isLoopback) aux(interfaces)
        else findIP4(interface.getInetAddresses) match {
          case None => aux(interfaces)
          case sa => sa
        }
      }
    aux(NetworkInterface.getNetworkInterfaces)
  }

  def findIP() : Unit = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    while(interfaces.hasMoreElements) {
      val interface : NetworkInterface = interfaces.nextElement()
      println(interface.getDisplayName)
      val addresses = interface.getInetAddresses
      while (addresses.hasMoreElements) {
        val address : InetAddress = addresses.nextElement
        println("\t" + address.getHostName + " " + address.getHostAddress)
      }
    }
  }
  def findPort() : Int = {
    val s = new ServerSocket(0)
    val p = s.getLocalPort
    s.close()
    p
  }

  def localIp = InetAddress.getLocalHost.getHostAddress

  def publicIp = {
    val whatismyip = new URL("http://checkip.amazonaws.com")
    val in = new BufferedReader(new InputStreamReader(whatismyip.openStream()))
    val ip = in.readLine() //you get the IP as a String
    in.close()
    ip
  }

  def myConfig(hostname : String, port : Int,
               bindHostname : String, bindPort : Int) =
    ConfigFactory.parseString("akka.remote.netty.tcp {\n" +
      s"hostname=$hostname\n"  +
      s"bind-hostname=$bindHostname\n" +
      s"port=$port\n" +
      s"bind-port=$bindPort\n" +
      "}"
    )


  def conf(confFileName : String) : Config = {

    val port = findPort()

    val regularConfig = ConfigFactory.load(confFileName)
    // override regular stack with myConfig
    val pIp = publicIp
    val lIp = findLocalIP getOrElse error("cannot find local ip")
    println("public ip = " + pIp)
    println("private ip = " + lIp)
    val combined = myConfig(pIp, port, lIp, port).withFallback(regularConfig)

    ConfigFactory.load(combined)
  }

  def main(args : Array[String]) : Unit = {
    println("--------findIP--------")
    findIP()
    println("#############")
    //val conf = ConfigFactory.load("client")
    val conf = ConnectionHelper.conf("client")

    val server = conf getString "leval.server.hostname"
    val serverPort = conf getString "leval.server.port"

    println(s"hostname = ${conf getString "akka.remote.netty.tcp.hostname"}")
    println(s"port = ${conf getString "akka.remote.netty.tcp.port"}")
    println(s"bind-hostname = ${conf getString "akka.remote.netty.tcp.bind-hostname"}")
    println(s"bind-port = ${conf getString "akka.remote.netty.tcp.bind-port"}")

    println(s"server = $server")
    println(s"serverPort = $serverPort")
  }
}

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

  //val conf = ConnectionHelper.conf("client")

  val server = conf getString "leval.server.hostname"
  val serverPort = conf getString "leval.server.port"

  println(s"hostname = ${conf getString "akka.remote.netty.tcp.hostname"}")
  println(s"port = ${conf getString "akka.remote.netty.tcp.port"}")
  println(s"bind-hostname = ${conf getString "akka.remote.netty.tcp.bind-hostname"}")
  println(s"bind-port = ${conf getString "akka.remote.netty.tcp.bind-port"}")

  println(s"server = $server")
  println(s"serverPort = $serverPort")

  val system = ActorSystem(systemName, conf)

  val serverPath = Settings.remotePath(server, serverPort)
  println(s"connecting to $serverPath")

  val control = new NetWorkController {
    val scene = stageScene
    system.actorOf(IdentifyingActor.props(serverPath, startMenuActor), actorName)
  }

  override def stopApp() : Unit = {
    control.disconnect()
    println("Shutting down !!")
    system.terminate()
    println("Bye bye !!")
    System.exit(0)
  }
}
