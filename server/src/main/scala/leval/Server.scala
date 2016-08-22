package leval

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import leval.network.Settings
import leval.network.server.EntryPoint

object Server {

  def main(args : Array[String]) : Unit = {
    start()
  }

  def start() : Unit = {
    val system = ActorSystem(Settings.systemName,
      ConfigFactory.load("server"))

    val _ = system.actorOf(EntryPoint.props, Settings.serverName)
  }
}
