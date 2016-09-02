package leval.serialization

import java.nio.ByteBuffer

import leval.core.{Being, Card, Suit}

/**
  * Created by Loïc Girault on 01/09/16.
  */
object BeingSerializer {

  def binarySize(b : Being) : Int =
    int + CardSerializer.cardSize +
      b.resources.size * (1 + CardSerializer.cardSize) + 1

  def put(bb : ByteBuffer, being : Being) : Unit = {
    bb putInt being.owner
    CardSerializer.put(bb, being.face)

    var numResourcesAndBools : Byte = being.resources.size.toByte
    numResourcesAndBools = (numResourcesAndBools << 2).toByte
    if(being.lover)
      numResourcesAndBools = (numResourcesAndBools | 0x01).toByte
    numResourcesAndBools = (numResourcesAndBools << 2).toByte
    if(being.hasDrawn)
      numResourcesAndBools = (numResourcesAndBools | 0x01).toByte

    bb put numResourcesAndBools

    being.resources.foreach{
      case (s, c) =>
        bb put (CardSerializer toByte s)
        CardSerializer.put(bb, c)
    }
  }
  def toBinary(being : Being) : Array[Byte] = {
    val bb = ByteBuffer allocate  binarySize(being)
    put(bb, being)
    bb.array()
  }

  def fromBinary(bytes : Array[Byte]) :  Being =
    fromBinary(ByteBuffer.wrap(bytes))

  def fromBinary(bb : ByteBuffer) :  Being = {
    val starIdx  = bb.getInt()
    val face = CardSerializer fromBinary bb
    var numResourcesAndBools = bb.get()
    val hasDrawn = (numResourcesAndBools & 0x01) == 0x01
    numResourcesAndBools = (numResourcesAndBools >> 2).toByte
    val isLover = (numResourcesAndBools & 0x01) == 0x01
    numResourcesAndBools = (numResourcesAndBools >> 2).toByte
    val numRessources = numResourcesAndBools.toInt

    val resources = (1 to numRessources).foldLeft(Map[Suit, Card]()){
      case (m, _) =>
        val s = CardSerializer suits bb.get().toInt
        val c = CardSerializer fromBinary bb
        m + ( s -> c)
    }

    Being(starIdx, face, resources, isLover, hasDrawn)
  }
}
