package leval.network

object Settings {
  val serverName = "SolarServer"
  val systemName = serverName + "System"
  //val serverAddress = "127.0.0.1"
  val serverAddress = "52.59.106.25"
  val remotePath =
    s"akka.tcp://$systemName@$serverAddress:2552/user/$serverName"

}
