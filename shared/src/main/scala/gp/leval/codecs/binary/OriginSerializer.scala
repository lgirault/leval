package gp.leval.codecs.binary

import gp.leval
import gp.leval.codecs.binary.{BeingSerializer, CardSerializer, byte}
import gp.leval.core.{CardOrigin, Origin}

import java.nio.ByteBuffer

/** Created by Loïc Girault on 01/09/16.
  */
object OriginSerializer {

  val starId: Byte = 0x00
  val handId: Byte = 0x01
  val beingId: Byte = 0x02

  def binarySize(origin: Origin): Int = origin match {
    case _: Origin.Star     => byte + int
    case _: CardOrigin.Hand => byte + int + CardSerializer.cardSize
    case CardOrigin.Being(b, _) =>
      byte + (BeingSerializer binarySize b) + CardSerializer.suitSize
  }

  def put(bb: ByteBuffer, origin: Origin): Unit = origin match {
    case Origin.Star(owner) =>
      bb put starId
      leval.ignore(bb putInt owner)
    case CardOrigin.Hand(owner, card) =>
      bb put handId
      bb putInt owner
      CardSerializer.put(bb, card)
    case CardOrigin.Being(being, s) =>
      bb put beingId
      BeingSerializer.put(bb, being)
      leval.ignore(bb put (CardSerializer toByte s))
  }

  def fromBinary(bb: ByteBuffer, kindId: Byte): CardOrigin =
    kindId match {
      case `handId` =>
        val owner = bb.getInt()
        val card = CardSerializer fromBinary bb
        CardOrigin.Hand(owner, card)

      case `beingId` =>
        val being = BeingSerializer fromBinary bb
        val s = CardSerializer.suits(bb.get.toInt)
        CardOrigin.Being(being, s)

      case _ => leval.error("unknown original kind")
    }

  def fromBinary(bb: ByteBuffer): Origin = {
    val kindId = bb.get()
    if (kindId == starId) Origin.Star(bb.getInt)
    else fromBinary(bb, kindId)

  }

}
