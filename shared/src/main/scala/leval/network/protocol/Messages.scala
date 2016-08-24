package leval.network.protocol

import akka.actor.ActorRef
import leval.core.{PlayerId, Rules}

abstract class Message

case class NetPlayerId
( actor : ActorRef,
  id : PlayerId) extends Message

case class GameDescription
(owner : NetPlayerId,
 rules : Rules) extends Message

sealed abstract class EntryPointRequest extends Message
case object ListGame extends EntryPointRequest
case class CreateGame
( desc : GameDescription)
  extends EntryPointRequest

case class GuestConnection
( login : String)
  extends EntryPointRequest

case class Connect
( login : String, password : String)
  extends EntryPointRequest


case class ConnectAck(id : PlayerId) extends Message
case class ConnectNack(msg : String) extends Message

case class Join(id : NetPlayerId) extends Message
case object GameStart extends Message


case object GameReady extends Message

case class WaitingPlayersGameInfo
( //makerRef : ActorRef,
  desc : GameDescription,
  currentNumPlayer : Int) extends Message

case class GameCreated( desc : GameDescription ) extends Message
//case object GameReady extends MapMakerAnswer
case class AckJoin(desc : GameDescription) extends Message
case object NackJoin extends Message

case class Disconnected(ref : NetPlayerId) extends Message