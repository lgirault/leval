package leval
package network
package client

import akka.actor._
import scala.concurrent.duration._


object IdentifyingActor {
  type IdReaction = (ActorContext, ActorRef) => Unit
  def props(serverPath : String,
            onIdentification : IdReaction) =
    Props(new IdentifyingActor(serverPath, onIdentification)).withDispatcher("javafx-dispatcher")

}
class IdentifyingActor private
( val serverPath : String,
  onIdentification : (ActorContext, ActorRef) => Unit)
  extends Actor {

  def sendIdentifyRequest() : Unit = {
    context.actorSelection(serverPath) ! Identify(serverPath)
    import context.dispatcher
    val _ = context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  sendIdentifyRequest()

  def receive = identifying

  def identifying: Actor.Receive = {
    case ActorIdentity(`serverPath`, Some(server)) =>
      println("In liaison with server")
      onIdentification(context, server)
      //context.watch(server)
      //context.become(active(server))
    case ActorIdentity(`serverPath`, None) => println(s"Server not available: $serverPath")
    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => println("Not ready yet")
  }




  def active(server: ActorRef): Actor.Receive = {
    case Disconnect =>
      print("Unwatching server ... ")
      context.unwatch(server)
      println("unwatching done")

    case Terminated(`server`) =>
      println("Server terminated")
      sendIdentifyRequest()
      context.become(identifying)

    case ReceiveTimeout =>
    // ignore
  }
}





