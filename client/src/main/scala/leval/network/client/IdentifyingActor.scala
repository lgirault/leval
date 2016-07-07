package leval
package network
package client

import akka.actor._
import scala.concurrent.duration._


object IdentifyingActor {
  def props(netHandle : NetWorkController) =
    Props(new IdentifyingActor(netHandle)).withDispatcher("javafx-dispatcher")
}
class IdentifyingActor private
( netHandle : NetWorkController)
  extends Actor {

  val path = Settings.remotePath

  def sendIdentifyRequest() : Unit = {
    context.actorSelection(path) ! Identify(path)
    import context.dispatcher
    val _ = context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  sendIdentifyRequest()

  def receive = identifying

  def identifying: Actor.Receive = {
    case ActorIdentity(`path`, Some(server)) =>
      println("In liaison with server")
      context.watch(server)

      val menuProps =
        MenuActor.props(server, netHandle)
          .withDispatcher("javafx-dispatcher")
      netHandle.actor = context.actorOf(menuProps)
      context.become(active(server))


    case ActorIdentity(`path`, None) => println(s"Server not available: $path")
    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => println("Not ready yet")
  }



  def active(server: ActorRef): Actor.Receive = {

    //TODO !!!
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





