package leval.network

object Settings {
  val serverName = "SolarServer"
  val systemName = serverName + "System"
  val remotePath =
    s"akka.tcp://$systemName@127.0.0.1:2552/user/$serverName"

}
