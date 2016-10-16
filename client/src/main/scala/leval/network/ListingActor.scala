package leval.network

import akka.actor.{Actor, ActorRef}

/**
  * Created by lorilan on 9/30/16.
  */
trait ListingActor  {
  this: MenuActor =>

  private var listeners = List[ListingListener]()

  def suscribe(listingListener: ListingListener) : Unit =
    listeners ::= listingListener

  def listing(serverRef : ActorRef) : Actor.Receive = {
    case PlayDescription(desc, currentNumPlayer) =>
      log info s"client receive game info from ${sender()}"

      val answer : JoinAction = {
        val s = sender()
        () => s ! Join(thisPlayer)
      }

      listeners foreach (l => l.handleDescription(desc, currentNumPlayer, answer))

    case JoinAck(GameDescription(creator, rules)) =>
      val waitingScreen = waitingOtherPlayerScreen(creator, rules)
      context.become( waitingPlayers( sender(), waitingScreen, creator ) )

    case JoinNack => println("Cannot join game")

    case StartScreen =>
      context.unbecome()
      displayStartScreen()

    case ListGame => serverRef ! ListGame

    case msg => log debug s"Listing state : msg $msg unhandled"
  }
}

trait ListingListener {
  def handleDescription(gameDescription: GameDescription, currentNumPlayer: Int, answer : JoinAction) : Unit
}