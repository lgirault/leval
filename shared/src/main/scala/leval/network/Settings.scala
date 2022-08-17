package leval.network

object Settings {
  val serverName = "SolarServer"
  val systemName = serverName + "System"

  def remotePath(serverAddress : String, port : String) =
    s"akka.tcp://$systemName@$serverAddress:$port/user/$serverName"


}
