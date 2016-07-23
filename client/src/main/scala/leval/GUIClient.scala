package leval

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import leval.gui.SearchingServerScene
import leval.network.client.{IdentifyingActor, NetWorkController}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene



object GUIClient extends JFXApp {

  val stageScene =  new Scene{
    root = new SearchingServerScene()
  }
  stage = new PrimaryStage {
    title = "Le Val"
    scene = stageScene
  }

  if(parameters.unnamed.isEmpty){
    scalafx.application.Platform.exit()
  }

  val port = parameters.unnamed.head.toInt

//  val (confFile, systemName, actorName) =
//      ("client", "ClientSystem", "IdentifyingActor")
//  println("using confFile " + confFile)
//  val conf = ConfigFactory.load(confFile)

  val (systemName, actorName) =
    (s"ClientSystem$port", "IdentifyingActor")


  def config(port: Int): Config = {
    val configStr =
     "akka.loglevel = OFF\n"+
     "javafx-dispatcher.type = Dispatcher\n" +
     "javafx-dispatcher.executor = " +
       "akka.dispatch.gui.JavaFXEventThreadExecutorServiceConfigurator\n" +
     "javafx-dispatcher.throughput = 1\n" +
      "akka.actor.provider = akka.remote.RemoteActorRefProvider \n" +
        "akka.remote.netty.tcp.hostname = 127.0.0.1\n" +
        "akka.remote.netty.tcp.port = " + port + "\n"

    ConfigFactory.parseString(configStr)
  }

  val conf = config(port)

  //conf.atKey("port")

  val system = ActorSystem(systemName, conf)

  def clientActor(netControl : NetWorkController) : ActorRef = {
    system.actorOf(IdentifyingActor.props(netControl), actorName)
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
  }
}
