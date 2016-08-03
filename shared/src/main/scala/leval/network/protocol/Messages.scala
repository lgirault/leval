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

case class Connect
( login : String, password : String)
  extends EntryPointRequest


case class ConnectAck(id : PlayerId)
case class ConnectNack(msg : String)

trait GameMakerRequest

case class Join(id : NetPlayerId) extends GameMakerRequest
case object GameStart extends GameMakerRequest


trait GameMakerAnswer
case object GameReady extends GameMakerAnswer

case class WaitingPlayersGameInfo
( //makerRef : ActorRef,
  desc : GameDescription,
  currentNumPlayer : Int)
  extends GameMakerAnswer

case class NewPlayer(ref : NetPlayerId) extends GameMakerAnswer
//case class GameCreationUpdate() extends ServerAnswer
case class GameCreated( desc : GameDescription ) extends GameMakerAnswer
//case object GameReady extends MapMakerAnswer
case class AckJoin(desc : GameDescription) extends GameMakerAnswer
case object NackJoin extends GameMakerAnswer

case class Disconnected(ref : NetPlayerId)