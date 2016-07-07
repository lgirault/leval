package leval.network.client

import akka.actor.{Actor, ActorRef, Props}
import leval.core.{Game, Move, PlayerId}
import leval.gui.{GameListPane, ViewController, WaitingRoom}
import leval.gui.gameScreen.ObservableGame
import leval.network.client.GameListView.JoinAction
import leval.network.protocol._

import scala.collection.mutable.ListBuffer

case object Disconnect

trait NetWorkController  {
  val view : ViewController


  var thisPlayer : PlayerId = _
  var actor : ActorRef = _

  lazy val netId = NetPlayerId(actor, thisPlayer)

  def connect(login : String, passWord : String) : Unit =
    actor ! Connect(login, passWord)

  def disconnect() : Unit =
    actor ! Disconnect

  def createGame(maxPlayer : Int) : Unit =
    actor ! CreateGame(GameDescription(netId, maxPlayer))

  def fetchGameList() : Unit = actor ! ListGame

  def startGame() : Unit = actor ! GameStart
}


trait Scheduler {
  this : Actor =>

  def scheduler(players : Seq[NetPlayerId],
                observableGame: ObservableGame) : Actor.Receive = {
    case m : Move[_] =>
      println(m + " received from " + context.sender())

      observableGame(m)

      if(context.sender() == context.system.deadLetters)
        players foreach {
          pid =>
            println("sending " + m + " to " + pid.actor)
          pid.actor ! m
        }
      else {
        println(context.sender() + " != " + Actor.noSender)
      }
  }
}

trait WaitinPlayers extends Scheduler {
  this : Actor =>
  def waitingPlayers( networkHandle: NetWorkController,
                      gameMaker : ActorRef,
                      waitingScreen : WaitingRoom,
                      owner : ActorRef) : Actor.Receive = {
    val players = ListBuffer[NetPlayerId]()

    {
      case NewPlayer(npid @ NetPlayerId(ref, pid)) =>
        players append npid
        println(s"new player : $npid")
        waitingScreen.addPlayer(pid)

      case GameReady =>
        val status =
          if (owner == self) Owner
          else Joiner

        waitingScreen.gameReady(networkHandle, status)

      case GameStart => gameMaker ! GameStart

      case g : Game =>
        println("launching game !")
        val og = new ObservableGame(g)
        waitingScreen.gameScreen(og)
        context.become(scheduler(players, og))



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
( serverEntryPoint : ActorRef,
  networkHandle : NetWorkController)
  extends Actor
    with WaitinPlayers {

  import networkHandle.view

  view.displayConnectScreen()
  
  def thisPlayer = networkHandle.netId//NetPlayerId(self, networkHandle.thisPlayer)

  def listing( gameListScreen : GameListPane ) : Actor.Receive = {
    case WaitingPlayersGameInfo(desc, currentNumPlayer) =>
      println(s"client receive game info from ${sender()}")
      //sender() ! Join(thisPlayer)

      val answer : JoinAction = {
        val s = sender()
        () => s ! Join(thisPlayer)
      }

      gameListScreen.appendGameToList(desc, currentNumPlayer, answer)

    case AckJoin(desc) =>
      val waitingScreen = view.waitingOtherPlayerScreen(desc.owner.id, desc.maxPlayer)
      context.become( waitingPlayers( networkHandle, sender(), waitingScreen, desc.owner.actor ) )

    case NackJoin => println("Cannot join game")
    case msg => println(s"Listing state : msg $msg unhandled")
  }



  def receive : Actor.Receive = {

    case ct : Connect =>
      serverEntryPoint ! ct

    case ConnectAck(pid) =>
      networkHandle.thisPlayer = pid
      val _ = view.displayStartScreen()

    case ConnectNack(msg) =>
      println(msg)

    case ListGame =>
      context.become( listing( view.gameListScreen() ) )
      serverEntryPoint ! ListGame
      
    case cg : CreateGame => serverEntryPoint ! cg

    case GameCreated(GameDescription(creator, maxPlayer)) =>
      val waitingScreen = view.waitingOtherPlayerScreen(creator.id, maxPlayer)
      context.become(waitingPlayers( networkHandle, sender(), waitingScreen, creator.actor ) )

    case req : EntryPointRequest => serverEntryPoint ! req

  }

}
