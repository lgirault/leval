package leval.network.client

import akka.actor.{Actor, ActorRef, Props, Terminated}
import leval.core.{BuryRequest, Game, Move, PlayerId, Rules, Twilight}
import leval.gui.{GameListPane, ViewController, WaitingRoom}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.gui.text.{Fr, ValText}
import leval.network.client.GameListView.JoinAction
import leval.network.protocol._

import scala.collection.mutable.ListBuffer

case object StartScreen
case object Disconnect

trait NetWorkController extends ViewController {

  var thisPlayer : PlayerId = _
  var actor : ActorRef = _

  implicit val text : ValText = Fr

  lazy val netId = NetPlayerId(actor, thisPlayer)

  def guestConnect(login : String) : Unit =
    actor ! GuestConnection(login)

  def connect(login : String, passWord : String) : Unit =
    actor ! Connect(login, passWord)

  def disconnect() : Unit =
    actor ! Disconnect

  def createGame(rules : Rules) : Unit =
    actor ! CreateGame(GameDescription(netId, rules))

  def fetchGameList() : Unit = actor ! ListGame

  def startGame() : Unit = actor ! GameStart
}


trait Scheduler {
  this : Actor =>
  def control: NetWorkController

  def scheduler(players : Seq[NetPlayerId],
                observableGame: ObservableGame,
                gameControl : GameScreenControl) : Actor.Receive = {
    case br @ BuryRequest(target, _) =>
      if(context.sender() == context.system.deadLetters) {
        val ownerId = observableGame.stars(target.owner).id.uuid
        players.find(_.id.uuid == ownerId) foreach (_.actor ! br)
      }
      else
        gameControl burry br

    case m : Move[_]  if context.sender() == context.system.deadLetters =>
      players foreach (_.actor ! m)
    case m : Move[_] =>
      println(m + " received from " + context.sender())
      leval.ignore(observableGame(m))

    case Terminated(ref) =>
      println()
      players.find(_.actor == ref) foreach {
        pid => gameControl.disconnectedPlayerAlert(pid.id.name)
      }
    case StartScreen =>
      players map (_.actor) foreach context.unwatch
      context.unbecome()
      control.displayStartScreen()
  }
}

trait WaitinPlayers extends Scheduler {
  this : Actor =>

  def thisPlayer : NetPlayerId
  def control: NetWorkController

  def waitingPlayers(gameMaker : ActorRef,
                     waitingScreen : WaitingRoom,
                     owner : NetPlayerId) : Actor.Receive = {
    val players = ListBuffer[NetPlayerId]()

    {
      case Join(npid @ NetPlayerId(ref, pid)) =>
        players append npid
        println(s"new player : $npid")
        waitingScreen addPlayer pid

      case GameReady =>
        val status =
          if (owner == thisPlayer) Owner
          else Joiner

        waitingScreen.gameReady(control, status)

      case GameStart => gameMaker ! GameStart

      case (t @ Twilight(_), g : Game) =>
        val og = new ObservableGame(g)
        players map (_.actor) foreach context.watch
        val control = waitingScreen.gameScreen(og)
        control.showTwilight(t)
        context.become(scheduler(players, og, control))

      case Disconnected(netId)  =>
        if(netId == owner) {
          waitingScreen.ownerExitAlert()
          self ! StartScreen
        }
        else {
          players.remove(players.indexOf(netId))
          waitingScreen.clearPlayers()
          players map (_.id) foreach waitingScreen.addPlayer
        }

      case StartScreen =>
        gameMaker ! Disconnected(thisPlayer)
        context.unbecome()
        control.displayStartScreen()


      case msg => println(s"Waiting players state : msg $msg unhandled")
    }
  }
}

object MenuActor {
  def props(serverEntryPoint : ActorRef,
            networkHandle : NetWorkController) =
    Props(new MenuActor(serverEntryPoint,
      networkHandle))
}

class MenuActor private
(serverEntryPoint : ActorRef,
 val control : NetWorkController)
  extends Actor
    with WaitinPlayers {

  control.displayConnectScreen()

  def thisPlayer : NetPlayerId = control.netId

  def listing( gameListScreen : GameListPane ) : Actor.Receive = {
    case WaitingPlayersGameInfo(desc, currentNumPlayer) =>
      println(s"client receive game info from ${sender()}")

      val answer : JoinAction = {
        val s = sender()
        () => s ! Join(thisPlayer)
      }

      gameListScreen.appendGameToList(desc, currentNumPlayer, answer)

    case AckJoin(GameDescription(creator, rules)) =>
      val waitingScreen = control.waitingOtherPlayerScreen(creator.id, rules)
      println("thisPlayer = " + thisPlayer + ", creator = " + creator+ ", equals ? = " + (creator == thisPlayer))
      context.become( waitingPlayers( sender(), waitingScreen, creator ) )

    case NackJoin => println("Cannot join game")

    case StartScreen => context.unbecome()

    case ListGame => serverEntryPoint ! ListGame

    case msg => println(s"Listing state : msg $msg unhandled")
  }



  def receive : Actor.Receive = {

    case ct : Connect =>
      serverEntryPoint ! ct

    case ConnectAck(pid) =>
      control.thisPlayer = pid
      val _ = control.displayStartScreen()

    case ConnectNack(msg) =>
      println(msg)

    case ListGame =>
      context.become( listing( control.gameListScreen() ) )
      serverEntryPoint ! ListGame

    case cg : CreateGame => serverEntryPoint ! cg

    case GameCreated(GameDescription(creator, rules)) =>
      val waitingScreen = control.waitingOtherPlayerScreen(creator.id, rules)
      context.become(waitingPlayers( sender(), waitingScreen, creator ) )

    case StartScreen => ()

    case req : EntryPointRequest => serverEntryPoint ! req

  }

}
