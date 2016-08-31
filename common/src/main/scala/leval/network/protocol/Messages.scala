package leval.network.protocol

import leval.core.{PlayerId, Rules}

abstract class Message

// 1 - Generic protocol

//initial handshake
case class GuestConnect
( clientVersion : String,
  login : String)
  extends Message

case class Connect
( clientVersion : String,
  login : String,
  password : String)
  extends Message


case class ConnectAck(id : PlayerId) extends Message
case class ConnectNack(msg : String) extends Message

//exit
case class Disconnect(pid : PlayerId) extends Message

//2 - Meta-Game protocol
case class GameDescription
(owner : PlayerId,
 rules : Rules) extends Message

// game creation
case class CreateGame
( desc : GameDescription)
extends Message
case class CreateGameAck(desc : GameDescription ) extends Message

// game start handshake
case object GameReady extends Message
case object GameStart extends Message


// game listing and joining
case object ListGame extends Message
case class PlayDescription
( desc : GameDescription,
  currentNumPlayer : Int) extends Message

case class Join(id : PlayerId) extends Message
case class JoinAck(desc : GameDescription) extends Message
case object JoinNack extends Message






