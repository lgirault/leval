package leval.network

import akka.pattern.ask
import akka.testkit.TestActorRef
import org.scalatest.concurrent.ScalaFutures
import leval.AcceptanceSpec
import leval.core.PlayerId
import leval.network.server.EntryPoint




class ProtocolSpec extends AcceptanceSpec with ScalaFutures with ActorSystemSetup {

  override val port: Int = 3232



  val serverRef = TestActorRef(new EntryPoint(0, 5))

  "The entry point" - {

    "given a valid (login, password) should acknowledge a connection" in {
      val answer = serverRef ? Connect("0.5", "Toto", "1234")
      whenReady(answer){
        case ConnectAck(PlayerId(_, "Toto")) => assert(true)
        case _ => assert(false, "ConnectAck was expected")
      }
    }

    "given a INvalid (login, password) should *NOT* acknowledge a connection" in {
      val answer = serverRef ? Connect("0.5", "Jack", "Bau-er")
      whenReady(answer){
        case ConnectNack(_) => assert(true)
        case _ => assert(false, "ConnectNack was expected")
      }
    }
  }

}
