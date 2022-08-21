package gp.leval

import gp.leval.core.PlayerId

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import gp.leval.core.PlayerId

/** Created by Loïc Girault on 31/08/16.
  */
package object serialization {
  val UTF_8 = StandardCharsets.UTF_8.name()
  val int = 4
  val byte = 1

  def getString(bb: ByteBuffer): String = {
    val length = bb.getInt()
    val bytes = new Array[Byte](length)
    bb get bytes
    new String(bytes, UTF_8)
  }

  def playerIdFromBinary(bb: ByteBuffer): PlayerId = {
    val uuid = bb.getInt()
    val name = getString(bb)
    PlayerId(uuid, name)
  }
  def playerIdFromBinary(bytes: Array[Byte]): PlayerId =
    playerIdFromBinary(ByteBuffer.wrap(bytes))

  def playerIdToBinary(p: PlayerId): Array[Byte] = {
    val nameb = p.name getBytes UTF_8

    val bb = ByteBuffer.allocate(int * 2 + nameb.length)
    bb putInt p.uuid
    bb putInt nameb.length
    bb put nameb
    bb.array()
  }
}
