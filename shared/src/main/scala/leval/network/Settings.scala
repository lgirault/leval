package leval.network

object Settings {
  val serverName = "SolarServer"
  val systemName = serverName + "System"

  def remotePath(serverAddress : String) =
    s"akka.tcp://$systemName@$serverAddress:2552/user/$serverName"

}
