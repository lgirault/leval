package leval.network

object Settings {
  val serverName = "TacticalServer"
  val remotePath =
    s"akka.tcp://TacticServerSystem@127.0.0.1:2552/user/$serverName"

}
