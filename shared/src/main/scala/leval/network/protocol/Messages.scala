package leval.network.protocol

import akka.actor.ActorRef
import leval.core.{PlayerId, Rules}

case class NetPlayerId
( actor : ActorRef,
  id : PlayerId)

case class GameDescription
(owner : NetPlayerId,
 rules : Rules)

trait EntryPointRequest
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


case class ConnectAck(id : PlayerId)
case class ConnectNack(msg : String)

case class Join(id : NetPlayerId)
case object GameStart


case object GameReady

case class WaitingPlayersGameInfo
( //makerRef : ActorRef,
  desc : GameDescription,
  currentNumPlayer : Int)

case class GameCreated( desc : GameDescription )
//case object GameReady extends MapMakerAnswer
case class AckJoin(desc : GameDescription)
case object NackJoin

case class Disconnected(ref : NetPlayerId)