package leval.network.client

import akka.actor.{Props, Actor, ActorRef}
import leval.core.PlayerId
import leval.gui.ViewController
import leval.network.client.GameListView.JoinAction
import leval.network.protocol._

import scala.collection.mutable.ListBuffer

case object Disconnect

trait GameLauncher {
  def startGame() : Unit
}

trait NetWorkController
  extends GameLauncher {
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



object MenuActor {

  def props(serverEntryPoint : ActorRef,
            networkHandle : NetWorkController) =
    Props(new MenuActor(serverEntryPoint,
                        networkHandle))

}

class MenuActor private
( serverEntryPoint : ActorRef,
  networkHandle : NetWorkController) extends Actor {

  import networkHandle.view

  view.displayConnectScreen()
  
  def thisPlayer = networkHandle.netId//NetPlayerId(self, networkHandle.thisPlayer)

  def listing( gameListScreen : GameListView ) : Actor.Receive = {
    case GameInfo(desc, currentNumPlayer) =>
      println(s"client receive game info from ${sender()}")
      //sender() ! Join(thisPlayer)

      val answer : JoinAction = {
        val s = sender()
        () => s ! Join(thisPlayer)
      }

      gameListScreen.appendGameToList(desc, currentNumPlayer, answer)

    case AckJoin(desc) =>
      val waitingScreen = view.waitingOtherPlayerScreen(desc.owner.id, desc.maxPlayer)
      context.become( waitingPlayers( sender(), waitingScreen, desc.owner.actor ) )

    case NackJoin => println("Cannot join game")
    case msg => println(s"Listing state : msg $msg unhandled")
  }

  def waitingScheduler(battleMapActor : ActorRef)  : Actor.Receive = {
    case sr @ SchedulerRef(ref) =>
      ref ! BattleMapActorRef(battleMapActor)
      battleMapActor ! sr
      context stop self

  }

  def waitingPlayers( gameMaker : ActorRef,
                      waitingScreen : WaitingOtherPlayerView,
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

      /*case md : MapDescription =>
        val controller = waitingScreen.gameScreen(md)
        val bmaProps = BattleMapActor.props(controller)
                          //.withDispatcher("javafx-dispatcher")

        val battleMapActor = context.system.actorOf(bmaProps, "BattleMapActor")

        controller.battleMapActor = Some(battleMapActor)

        if(owner == self) {
          val schedulerProps = GameScheduler.props(players.size + 1)

          val schedulerActor = context.system.actorOf(schedulerProps, "Scheduler")

          (thisPlayer +: players).toList foreach {
            _.actor ! SchedulerRef
          }

        }

        context.become(waitingScheduler(battleMapActor))*/



      case msg => println(s"Waiting players state : msg $msg unhandled")
    }
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
      context.become(waitingPlayers( sender(), waitingScreen, creator.actor ) )

    case req : EntryPointRequest => serverEntryPoint ! req

  }

}
