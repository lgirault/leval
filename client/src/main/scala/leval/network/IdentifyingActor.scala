package leval.network

import akka.actor._

import scala.concurrent.duration._
import scalafx.scene.Scene

object IdentifyingActor {
  type IdReaction = (ActorContext, ActorRef) => Unit
  def props(serverPath : String,
            scene : Scene) =
    Props(new IdentifyingActor(serverPath, scene)).withDispatcher("javafx-dispatcher")

}
class IdentifyingActor private
( val serverPath : String,
  scene : Scene)
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
      context actorOf MenuActor.props(scene, server)
      context become passive
      //context.watch(server)
      //context.become(active(server))
    case ActorIdentity(`serverPath`, None) => println(s"Server not available: $serverPath")
    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => println("Not ready yet")
  }




  def active(server: ActorRef): Actor.Receive = {
    case Disconnect(_) =>
      context.unwatch(server)

    case Terminated(`server`) =>
      sendIdentifyRequest()
      context.become(identifying)

    case ReceiveTimeout =>
    // ignore
  }
}





