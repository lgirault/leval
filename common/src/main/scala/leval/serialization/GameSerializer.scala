package leval.serialization

import java.nio.ByteBuffer

import akka.serialization.SerializerWithStringManifest
import leval.core._

/**
  * Created by Loïc Girault on 31/08/16.
  */
object GameSerializer {

  val ruleSize = 1
  def rulesToBinary(r : Rules): Array[Byte] = {
    val rid : Byte = r match {
      case Sinnlos => 0x00
      case Antares => 0x01
      case Helios =>  0x02
    }

    Array[Byte](rid)
  }

  private val rules = Array(Sinnlos, Antares, Helios)

  def getRules(bb : ByteBuffer) : Rules =
    rules(bb.get().toInt)

}

object GameManifest{
  val game = "game"

  // moves
  val majestyEffect = "majestyEffect"
  val attackBeing = "attackBeing"
  val removeFromHand = "removeFromHand"
  val activateBeing = "activateBeing"
  val collect = "collect"
  val reveal = "reveal"
  val lookCard = "lookCard"
  val placeBeing = "placeBeing"
  val bury = "bury"
  val buryRequest = "buryRequest"
  val educateSwitch = "educateSwitch"
  val educateRise = "educateRise"
  val influencePhase = "influencePhase"
  val actPhase = "actPhase"
  val sourcePhase = "sourcePhase"
  val twilight = "twilight"

}


class GameSerializer
  extends SerializerWithStringManifest {

  def identifier: Int = 79658247

  def manifest(o: AnyRef): String = ???

  def toBinary(o: AnyRef): Array[Byte] = ???

  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = ???
}

