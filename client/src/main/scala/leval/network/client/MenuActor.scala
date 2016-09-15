package leval.network.client

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.event.Logging
import com.typesafe.config.Config
import leval.core.{BuryRequest, GameInit, InfluencePhase, Move, OsteinSelection, PlayerId, Rules, Twilight}
import leval.gui.{GameListPane, ViewController, WaitingRoom}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame, OsteinHandler}
import leval.gui.text.ValText
import leval.network._
import leval.network.client.GameListView.JoinAction


case object StartScreen

trait NetWorkController extends ViewController {

  var config : Config
  val majorVersion : Int
  val minorVersion : Int

  var thisPlayer : PlayerId = _
  private [this] var actor0 : ActorRef = _
  def actor : ActorRef = actor0
  def actor_=(r : ActorRef) = {
    actor0 = r
  }

  import leval.LevalConfig._

  implicit def texts : ValText = config.lang()

  def startMenuActor(context : ActorContext, serverRef: ActorRef) : Unit = {
    val menuProps =
      MenuActor.props(serverRef, this)
        .withDispatcher("javafx-dispatcher")
    this.actor = context.actorOf(menuProps)
    context become passive
  }

  def guestConnect(login : String) : Unit =
    actor ! GuestConnect(s"$majorVersion.$minorVersion", login)

  def connect(login : String, passWord : String) : Unit =
    actor ! Connect(s"$majorVersion.$minorVersion", login, passWord)

  def disconnect() : Unit =
    actor ! Disconnect(thisPlayer)

  def createGame(rules : Rules) : Unit =
    actor ! CreateGame(GameDescription(thisPlayer, rules))

  def fetchGameList() : Unit = actor ! ListGame

  def startGame() : Unit = actor ! GameStart
}



trait InGame {
  this : Actor =>
  def control: NetWorkController

  val log = Logging.getLogger(context.system, this)

  def ingame(scheduler : ActorRef,
             observableGame: ObservableGame,
             gameControl : GameScreenControl) : Actor.Receive = {
    case br @ BuryRequest(target, _) =>
      if(context.sender() == context.system.deadLetters)
        scheduler ! br
      else
        gameControl burry br

    case m : Move[_]  =>
      if (context.sender() == context.system.deadLetters)
        scheduler ! m

      leval.ignore(observableGame(m))

    case Disconnect(pid) =>
      gameControl.disconnectedPlayerAlert(pid.name)

    case StartScreen =>
      scheduler ! Disconnect(control.thisPlayer)
      context.unbecome()
      control.displayStartScreen()
  }
}

trait Drafting extends InGame {
  this: Actor =>
  def control: NetWorkController
  def drafting(scheduler : ActorRef,
               observableGame: ObservableGame,
               handler : OsteinHandler) : Actor.Receive = {
    case os @ OsteinSelection(c) =>
      if(context.sender() == context.system.deadLetters)
        scheduler ! os
      else
        handler opponentPick c

    case t @ Twilight(_) =>
      context.unbecome()
      handler.control.showTwilight(t)
      context.become(ingame(scheduler, observableGame, handler.control))
  }
}

trait WaitinPlayers extends Drafting {
  this : Actor =>

  def waitingPlayers(gameMaker : ActorRef,
                     waitingScreen : WaitingRoom,
                     owner : PlayerId) : Actor.Receive = {

    {
      case Join(pid) =>
        log info s"new player : $pid"
        waitingScreen addPlayer pid

      case GameReady =>
        val status =
          if (owner == control.thisPlayer) Owner
          else Joiner

        waitingScreen.gameReady(control, status)

      case GameStart =>
        println("sending gameStart !")
        gameMaker ! GameStart

      case gi : GameInit   =>
        println("gameInit received")
        val g =
          if(gi.rules.ostein)
            gi.game.copy(currentPhase = InfluencePhase(-1))
          else gi.game

        val og = new ObservableGame(g)
        val gameControl = control.gameScreen(og)

        if(gi.rules.ostein) {
          val oh = new OsteinHandler(gameControl)
          oh.start()
          println("oh oh oh !")
          context.become(drafting(gameMaker, og, oh))
        }
        else {
          gameControl.showTwilight(gi.twilight)
          context.become(ingame(gameMaker, og, gameControl))
        }




      case Disconnect(netId)  =>
        if(netId == owner) {
          waitingScreen.ownerExitAlert()
          self ! StartScreen
        }
        else
          waitingScreen rmPlayer netId


      case StartScreen =>
        gameMaker ! Disconnect(control.thisPlayer)
        context.unbecome()
        control.displayStartScreen()


      case msg => log debug s"Waiting players state : msg $msg unhandled"
    }
  }
}

object MenuActor {
  def props(serverRef : ActorRef,
            networkHandle : NetWorkController) =
    Props(new MenuActor(serverRef, networkHandle))
}

class MenuActor private
(serverRef : ActorRef,
 val control : NetWorkController)
  extends Actor
    with WaitinPlayers {

  control.displayConnectScreen()

  def listing( gameListScreen : GameListPane ) : Actor.Receive = {
    case PlayDescription(desc, currentNumPlayer) =>
      log info s"client receive game info from ${sender()}"

      val answer : JoinAction = {
        val s = sender()
        () => s ! Join(control.thisPlayer)
      }

      gameListScreen.appendGameToList(desc, currentNumPlayer, answer)

    case JoinAck(GameDescription(creator, rules)) =>
      val waitingScreen = control.waitingOtherPlayerScreen(creator, rules)
      context.become( waitingPlayers( sender(), waitingScreen, creator ) )

    case JoinNack => println("Cannot join game")

    case StartScreen => context.unbecome()

    case ListGame => serverRef ! ListGame

    case msg => log debug s"Listing state : msg $msg unhandled"
  }



  def receive : Actor.Receive = {

    case ConnectAck(pid) =>
      control.thisPlayer = pid
      leval.ignore(control.displayStartScreen())

    case ConnectNack(msg) =>
      leval.ignore(control.connectError(msg))

    case ListGame =>
      context.become( listing( control.gameListScreen() ) )
      serverRef ! ListGame

    case cg : CreateGame => serverRef ! cg

    case CreateGameAck(GameDescription(creator, rules)) =>
      val waitingScreen = control.waitingOtherPlayerScreen(creator, rules)
      context.become(waitingPlayers( sender(), waitingScreen, creator ) )

    case StartScreen => ()

    case req : Message => serverRef ! req

  }

}
