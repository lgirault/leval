package leval.network.protocol

import java.nio.charset.StandardCharsets
import java.nio.ByteBuffer

import akka.serialization.SerializerWithStringManifest
import leval.core.PlayerId

/**
  * Created by lorilan on 8/28/16.
  */
object MessageManifest {
  val guestConnect = "guestConnect"
  val connect = "connect"
  val connectAck = "connectAck"
  val connectNack = "connectNack"
}
class MessagesSerializer
  extends SerializerWithStringManifest{

  def identifier: Int = 79658247
  val UTF_8 = StandardCharsets.UTF_8.name()
  val int = 4

  def manifest(o: AnyRef): String = o match {
    case _ : GuestConnect => MessageManifest.guestConnect
    case _ : Connect => MessageManifest.connect
    case _ : ConnectAck => MessageManifest.connectAck
    case _ : ConnectNack => MessageManifest.connectNack
  }

  def toBinary(o: AnyRef): Array[Byte] = o match {
    case GuestConnect(clientVersion, login) =>
      val cvb = clientVersion.getBytes(UTF_8)
      val lb = login.getBytes(UTF_8)

      val bb = ByteBuffer.allocate( int * 2 + cvb.length + lb.length)
      bb putInt cvb.length
      bb put cvb
      bb putInt lb.length
      bb put lb
      bb.array()

    case ConnectAck(PlayerId(uuid, name)) =>
      val nameb = name.getBytes(UTF_8)
      val bb = ByteBuffer.allocate( int * 2 + nameb.length)

      bb putInt uuid
      bb putInt nameb.length
      bb put nameb

      bb.array()

    case ConnectNack(msg) =>
      val msgb = msg.getBytes(UTF_8)
      val bb = ByteBuffer.allocate( int + msgb.length)

      bb putInt msgb.length
      bb put msgb

      bb.array()

  }

  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case MessageManifest.guestConnect =>
      val bb = ByteBuffer.wrap(bytes)
      val cvs =  bb.getInt()
      val clientVersion = new Array[Byte](cvs)
      bb get clientVersion
      val ls =  bb.getInt()
      val login = new Array[Byte](ls)
      bb get login
      GuestConnect(new String(clientVersion, UTF_8), new String(login, UTF_8))

    case MessageManifest.connectAck =>
      val bb = ByteBuffer.wrap(bytes)
      val uuid =  bb.getInt()
      val ls =  bb.getInt()
      val login = new Array[Byte](ls)
      bb get login
      ConnectAck(PlayerId(uuid, new String(login, UTF_8)))


    case MessageManifest.connectNack =>
      val bb = ByteBuffer.wrap(bytes)
      val ms =  bb.getInt()
      val msg = new Array[Byte](ms)
      bb get msg
      ConnectNack(new String(msg, UTF_8))

  }
}
