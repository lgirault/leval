package leval.network

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import leval.core.{BuryRequest, Move}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}

/**
  * Created by lorilan on 9/30/16.
  */
trait InGameActor {
  this : MenuActor =>

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
      scheduler ! Disconnect(thisPlayer)
      context.unbecome()
      displayStartScreen()
  }
}
