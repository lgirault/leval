package leval.network.protocol

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import akka.serialization._
import leval.core.PlayerId
import leval.network.GameSerializer

/**
  * Created by lorilan on 8/28/16.
  */
object MessageManifest {
  // 1 - Generic protocol
  val playerId = "playerId"

  //initial handshake
  val guestConnect = "guestConnect"
  val connect = "connect"

  val connectAck = "connectAck"
  val connectNack = "connectNack"

  //exit
  val disconnect = "disconnect"

  //2 - Meta-Game protocol

  val gameDescription = "gameDescription"

  // game creation

  val createGame = "createGame"
  val createGameAck = "createGameAck"

  // game start handshake
  val gameReady = "gameReady"
  val gameStart = "gameStart"

  // game listing and joining
  val listGame = "listGame"
  val playDescription = "playDescription"

  val join = "join"
  val joinAck = "joinAck"
  val joinNack = "joinNack"


}



class MessagesSerializer
  extends SerializerWithStringManifest {

  def identifier: Int = 79658247
  val UTF_8 = StandardCharsets.UTF_8.name()
  val int = 4

  def manifest(o: AnyRef): String = o match {
    case _ : PlayerId => MessageManifest.playerId

    case _ : GuestConnect => MessageManifest.guestConnect
    case _ : Connect => MessageManifest.connect
    case _ : ConnectAck => MessageManifest.connectAck
    case _ : ConnectNack => MessageManifest.connectNack

    case _ : Disconnect => MessageManifest.disconnect

    case _ : GameDescription => MessageManifest.gameDescription
    case _ : CreateGame => MessageManifest.createGame
    case _ : CreateGameAck => MessageManifest.createGameAck
    case GameReady => MessageManifest.gameReady
    case GameStart => MessageManifest.gameStart

    case ListGame => MessageManifest.listGame
    case _ : PlayDescription => MessageManifest.playDescription
    case _ : Join => MessageManifest.join
    case _ : JoinAck => MessageManifest.joinAck
    case JoinNack => MessageManifest.joinNack
  }


  def playerIdToBinary(p : PlayerId) : Array[Byte] = {
    val nameb = p.name getBytes UTF_8

    val bb = ByteBuffer.allocate( int * 3 + nameb.length)
    bb putInt p.uuid
    bb putInt nameb.length
    bb put nameb
    bb.array()
  }

  def toBinary(o: AnyRef): Array[Byte] = o match {
    case p : PlayerId => playerIdToBinary(p)

    case GuestConnect(clientVersion, login) =>
      val cvb = clientVersion.getBytes(UTF_8)
      val lb = login.getBytes(UTF_8)

      val bb = ByteBuffer.allocate( int * 2 + cvb.length + lb.length)
      bb putInt cvb.length
      bb put cvb
      bb putInt lb.length
      bb put lb
      bb.array()

    case ConnectAck(p) => playerIdToBinary(p)

    case ConnectNack(msg) =>
      val msgb = msg.getBytes(UTF_8)
      val bb = ByteBuffer.allocate( int + msgb.length)

      bb putInt msgb.length
      bb put msgb

      bb.array()

    case Disconnect(pid) => toBinary(pid)

    case GameDescription(p, r) =>
      val pid = playerIdToBinary(p)
      val bb = ByteBuffer.allocate(pid.length + GameSerializer.ruleSize)
      bb put pid
      bb put GameSerializer.rulesToBinary(r)
      bb.array()

    case CreateGame(desc) => toBinary(desc)
    case CreateGameAck(desc) => toBinary(desc)

    case GameReady => Array.empty
    case GameStart => Array.empty

    case ListGame => Array.empty

    case PlayDescription(desc, numPlayer) =>
      val bdesc = toBinary(desc)
      val bb = ByteBuffer.allocate(bdesc.length + int)
      bb put bdesc
      bb putInt numPlayer
      bb.array()

    case Join(pid) => playerIdToBinary(pid)
    case JoinAck(desc) => toBinary(desc)

    case JoinNack => Array.empty
  }


  def getString(bb : ByteBuffer) : String = {
    val length = bb.getInt()
    val bytes = new Array[Byte](length)
    bb get bytes
    new String(bytes, UTF_8)
  }

  def playerIdFromBinary(bb: ByteBuffer) : PlayerId = {
    val uuid = bb.getInt()
    val name = getString(bb)
    PlayerId(uuid, name)
  }
  def playerIdFromBinary(bytes: Array[Byte]) : PlayerId =
    playerIdFromBinary(ByteBuffer.wrap(bytes))

  def gameDescFromBinary(bb: ByteBuffer) : GameDescription = {
    val pid = playerIdFromBinary(bb)
    val rules = GameSerializer.getRules(bb)
    GameDescription(pid, rules)
  }

  def gameDescFromBinary(bytes: Array[Byte]) : GameDescription =
    gameDescFromBinary(ByteBuffer.wrap(bytes))


  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case MessageManifest.playerId =>
      playerIdFromBinary(bytes)

    case MessageManifest.guestConnect =>
      val bb = ByteBuffer.wrap(bytes)
      val clientVersion = getString(bb)
      val login = getString(bb)
      GuestConnect(clientVersion, login)

    case MessageManifest.connectAck =>
      ConnectAck(playerIdFromBinary(bytes))

    case MessageManifest.connectNack =>
      val bb = ByteBuffer.wrap(bytes)
      val ms =  bb.getInt()
      val msg = new Array[Byte](ms)
      bb get msg
      ConnectNack(new String(msg, UTF_8))

    case MessageManifest.disconnect =>
      Disconnect(playerIdFromBinary(bytes))


    case MessageManifest.gameDescription =>
      gameDescFromBinary(bytes)

    case MessageManifest.createGame =>
      CreateGame(gameDescFromBinary(bytes))
    case MessageManifest.createGameAck =>
      CreateGameAck(gameDescFromBinary(bytes))

    case MessageManifest.gameReady =>
      GameReady
    case MessageManifest.gameStart =>
      GameStart

    case MessageManifest.listGame =>
      ListGame

    case MessageManifest.playDescription =>
      val bb = ByteBuffer.wrap(bytes)
      val desc = gameDescFromBinary(bb)
      PlayDescription(desc, bb.getInt)

    case MessageManifest.join =>
      Join(playerIdFromBinary(bytes))

    case MessageManifest.joinAck =>
      JoinAck(gameDescFromBinary(bytes))

    case MessageManifest.joinNack =>
      JoinNack
  }
}
