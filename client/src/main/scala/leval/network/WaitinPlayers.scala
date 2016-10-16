package leval.network

import akka.actor.{Actor, ActorRef}
import leval.core.{GameInit, PlayerId}
import leval.gui.WaitingRoom
import leval.gui.gameScreen.{ObservableGame, OsteinHandler}

/**
  * Created by lorilan on 9/30/16.
  */
trait WaitinPlayers {
  this : MenuActor =>

  def waitingPlayers(gameMaker : ActorRef,
                     waitingScreen : WaitingRoom,
                     owner : PlayerId) : Actor.Receive = {

    {
      case Join(pid) =>
        log info s"new player : $pid"
        waitingScreen addPlayer pid

      case GameReady =>
        waitingScreen.gameReady(self, owner == thisPlayer)

      case GameStart =>
        gameMaker ! GameStart

      case gi : GameInit   =>
        val og = new ObservableGame(gi.game)
        val gameControl = gameScreen(og)

        if(gi.rules.ostein) {
          val oh = new OsteinHandler(gameControl)
          oh.start()
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
        gameMaker ! Disconnect(thisPlayer)
        context.unbecome()
        displayStartScreen()


      case msg => log debug s"Waiting players state : msg $msg unhandled"
    }
  }
}
