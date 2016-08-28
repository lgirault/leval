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
    val conf =  ConfigFactory load "server"
    val system = ActorSystem(Settings.systemName, conf)

    val majorVersion : Int = conf getInt "leval.client.version.major"
    val minorVersion : Int = conf getInt "leval.client.version.minor"

    val _ = system.actorOf(EntryPoint.props(majorVersion, minorVersion),
      Settings.serverName)
  }
}
