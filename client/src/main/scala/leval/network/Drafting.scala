package leval.network

import akka.actor.{Actor, ActorRef}
import leval.core.{OsteinSelection, Twilight}
import leval.gui.gameScreen.{ObservableGame, OsteinHandler}

/**
  * Created by lorilan on 9/30/16.
  */
trait Drafting  {
  this: MenuActor =>

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
