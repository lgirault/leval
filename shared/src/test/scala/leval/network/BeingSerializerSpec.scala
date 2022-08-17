package leval.network

import leval.AcceptanceSpec
import leval.TestSamples._
/**
  * Created by lorilan on 9/1/16.
  */
class BeingSerializerSpec
  extends AcceptanceSpec {

  import leval.serialization.BeingSerializer._
  "A being serializer" - {
    "should encode and decode a child" in {
      val b = toBinary(child)
      fromBinary(b) shouldBe child
    }
    "should encode and decode a wizard" in {
      val b = toBinary(wizard)
      fromBinary(b) shouldBe wizard
    }
    "should encode and decode a spectre" in {
      val b = toBinary(spectre)
      fromBinary(b) shouldBe spectre
    }
    "should encode and decode a blackLady" in {
      val b = toBinary(blackLady)
      fromBinary(b) shouldBe blackLady
    }
    "should encode and decode a fool" in {
      val b = toBinary(fool)
      fromBinary(b) shouldBe fool
    }
  }

}
