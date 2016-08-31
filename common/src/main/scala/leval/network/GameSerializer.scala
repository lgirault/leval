package leval.network

import java.nio.ByteBuffer

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
