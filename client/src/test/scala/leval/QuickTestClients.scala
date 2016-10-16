/*
package leval

import akka.actor.{Actor, ActorContext, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import leval.control.MenuController
import leval.gui.SearchingServerScene
import leval.network._
import leval.core._
import leval.gui.gameScreen.{ObservableGame, OsteinHandler}

import scala.collection.mutable.ListBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

/**
  * Created by lorilan on 8/27/16.
  */

trait WaitinPlayersTest
  extends Actor
    with Drafting {
  val control : MenuController

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

      case gi : GameInit =>

        val og = new ObservableGame(gi.game)
        val gameControl = control.gameScreen(og)

        if(gi.rules.ostein) {
          val oh = new OsteinHandler(gameControl)
          oh.start()
          context.become(drafting(gameMaker, og, oh))
        }
        else {
          gameControl.showTwilight(gi.twilight)
          context.become(ingame(gameMaker, og, gameControl))
        }

      case Disconnect(netId)  => println(netId + " disconnected")

      case msg => println(s"Waiting players state : msg $msg unhandled")
    }
  }

}

object CreatorActor {
  def props(serverRef : ActorRef,
            control : MenuController) =
    Props(new CreatorActor(serverRef, control)).withDispatcher("javafx-dispatcher")

}
class CreatorActor
(serverRef : ActorRef,
 val control : MenuController)
  extends WaitinPlayersTest {

  def receive: Receive = {
    case cg : CreateGame => serverRef ! cg
    case CreateGameAck(GameDescription(creator, rules)) =>
      context become waitingPlayers(sender(), control.thisPlayer)
  }
}
object JoiningActor {
  def props(serverRef : ActorRef,
            control : MenuController) =
    Props(new JoiningActor(serverRef, control)).withDispatcher("javafx-dispatcher")

}
class JoiningActor
(serverRef : ActorRef,
 val control : MenuController)
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

  def startTestActor(control : MenuController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit

  val onTestActorCreate : MenuController => Unit

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

  val server = "127.0.0.1"//conf getString "leval.server.hostname"
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

  val control = new MenuController() {
    override def actor_=(r: ActorRef): Unit = {
      super.actor_=(r)
      thisPlayer = PlayerId(uid, name)
      onTestActorCreate(this)
    }
  }
  control.system = system
  override def stopApp() : Unit = control.exit()

  import LevalConfig.Keys
  control.majorVersion = conf getInt Keys.majorVersion
  control.minorVersion = conf getInt Keys.minorVersion
  control.scene = stageScene
  control.config = LevalConfig.default

  system.actorOf(IdentifyingActor.props(serverPath, startTestActor(control)), actorName)

}

object QuickCreatorClient extends QuickTestClient {

  val uid = 0
  val name = "Creator"
  val port: Int = 1234

  def startTestActor(control : MenuController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit = {
    val menuProps =
      CreatorActor.props(serverRef, control)
        .withDispatcher("javafx-dispatcher")
    control.actor = context.actorOf(menuProps)
    context become passive
  }

  val onTestActorCreate: (MenuController) => Unit = {
    ctrl => ctrl.createGame(Rules(Antares, ostein = true))
  }
}

object QuickJoiningClient extends QuickTestClient {
  val uid = 1
  val name = "Joiner"
  val port: Int = 4567
  def startTestActor(control : MenuController)
                    (context : ActorContext,
                     serverRef: ActorRef) : Unit = {
    val menuProps =
      JoiningActor.props(serverRef, control)
        .withDispatcher("javafx-dispatcher")
    control.actor = context.actorOf(menuProps)
    context become passive
  }
  val onTestActorCreate: (MenuController) => Unit = {
    ctrl => ctrl.fetchGameList()
  }
}*/
