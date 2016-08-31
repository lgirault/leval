package leval

import akka.actor.{Actor, ActorContext, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import leval.gui.SearchingServerScene
import leval.network._
import leval.network.client._
import leval.core.{Game, PlayerId, Sinnlos, Twilight}
import leval.gui.gameScreen.ObservableGame

import scala.collection.mutable.ListBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

/**
  * Created by lorilan on 8/27/16.
  */

trait WaitinPlayersTest
  extends Actor
    with InGame {
  val control : NetWorkController

  def waitingPlayers(gameMaker : ActorRef,
                     owner : PlayerId) : Actor.Receive = {
    val players = ListBuffer[PlayerId]()

    {
      case Join(pid) =>
        players append pid
        println(s"new player : $pid")

      case GameReady =>
        val status =
          if (owner == control.thisPlayer) {
            gameMaker ! GameStart
          }

      case (t @ Twilight(_), g : Game) =>
        val og = new ObservableGame(g)
        val gameControl = control.gameScreen(og)
        gameControl.showTwilight(t)
        context.become(ingame(gameMaker, og, gameControl))

      case Disconnect(netId)  => println(netId + " disconnected")

      case msg => println(s"Waiting players state : msg $msg unhandled")
    }
  }

}

object CreatorActor {
  def props(serverRef : ActorRef,
            control : NetWorkController) =
    Props(new CreatorActor(serverRef, control)).withDispatcher("javafx-dispatcher")

}
class CreatorActor
(serverRef : ActorRef,
 val control : NetWorkController)
  extends WaitinPlayersTest {

  def receive: Receive = {
    case cg : CreateGame => serverRef ! cg
    case CreateGameAck(GameDescription(creator, rules)) =>
      context become waitingPlayers(sender(), control.thisPlayer)
  }
}
object JoiningActor {
  def props(serverRef : ActorRef,
            control : NetWorkController) =
    Props(new JoiningActor(serverRef, control)).withDispatcher("javafx-dispatcher")

}
class JoiningActor
(serverRef : ActorRef,
 val control : NetWorkController)
  extends WaitinPlayersTest {

  def receive: Receive = {
    case ListGame => serverRef ! ListGame
    case PlayDescription(desc, currentNumPlayer) =>
      println("WaitingPlayersGameInfo received")
      sender() ! Join(control.thisPlayer)
    case JoinAck(GameDescription(creator, rules)) =>
      context.become( waitingPlayers( sender(), creator ) )
  }
}

abstract class QuickTestClient extends JFXApp {

  val uid : Int
  val name : String
  val port : Int

  def startTestActor(control : NetWorkController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit

  val onTestActorCreate : NetWorkController => Unit

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

  val ip ="127.0.0.1"

  val regularConfig = ConfigFactory.load("client")
  val conf = ConnectionHelper.myConfig(ip, port, ip, port).withFallback(regularConfig)

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

  override def stopApp() : Unit = {
    control.disconnect()
    println("Shutting down !!")
    system.terminate()
    println("Bye bye !!")
    System.exit(0)
  }

  val control = new NetWorkController {
    val majorVersion: Int = conf getInt "leval.client.version.major"
    val minorVersion: Int = conf getInt "leval.client.version.minor"
    def exit() = stopApp()


    val scene = stageScene

    system.actorOf(IdentifyingActor.props(serverPath, startTestActor(this)), actorName)

    override def actor_=(r: ActorRef): Unit = {
      super.actor_=(r)
      thisPlayer = PlayerId(uid, name)
      onTestActorCreate(this)
    }
  }


}

object QuickCreatorClient extends QuickTestClient {

  val uid = 0
  val name = "Creator"
  val port: Int = 1234

  def startTestActor(control : NetWorkController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit = {
    val menuProps =
      CreatorActor.props(serverRef, control)
        .withDispatcher("javafx-dispatcher")
    control.actor = context.actorOf(menuProps)
    context become passive
  }

  val onTestActorCreate: (NetWorkController) => Unit = {
    ctrl => ctrl.createGame(Sinnlos)
  }
}

object QuickJoiningClient extends QuickTestClient {
  val uid = 1
  val name = "Joiner"
  val port: Int = 4567
  def startTestActor(control : NetWorkController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit = {
    val menuProps =
      JoiningActor.props(serverRef, control)
        .withDispatcher("javafx-dispatcher")
    control.actor = context.actorOf(menuProps)
    context become passive
  }
  val onTestActorCreate: (NetWorkController) => Unit = {
    ctrl => ctrl.fetchGameList()
  }
}