package leval.network

import akka.testkit.{TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import leval.AcceptanceSpec
import leval.core.{PlayerId, Rules, Sinnlos}
import leval.network.server.GameMaker

import scala.concurrent.duration._

class GameMakerSpec
  extends AcceptanceSpec
  with ScalaFutures
  with ActorSystemSetup
  with BeforeAndAfterEach {

  val port = 3233

  val systemConfig : String = "server"

  val totoId = PlayerId(0, "Toto")
  val titiId = PlayerId(1, "Titi")
  val tutuId = PlayerId(2, "Tutu")


  val gameMakerRef = TestActorRef(new GameMaker(???,
    GameDescription( totoId, Rules(Sinnlos))))
  val gameMaker = gameMakerRef.underlyingActor

  val mapSize = 5
  val maxPlayer = 3



//  override def afterEach() {
//  }


  "A game maker" - {

    /*"given a game creation request initialize itself with the game config" in {

      gameMaker.maxNumPlayer = 0

      val clientRef = TestProbe()

      val gameDesc = GameDescription(totoId, maxPlayer)

      gameMakerRef ! Delegate(clientRef.ref, CreateGame(gameDesc))

      clientRef.expectMsg(100 millis, GameCreated(gameDesc))

      gameMaker.maxNumPlayer shouldBe maxPlayer
    }*/

    "given a joining player the game owner remains the same" in {

      val ownerProbe = TestProbe()
      val joinerProbe = TestProbe()
      //val totoNetId= NetPlayerId(ownerProbe.ref, totoId)
      //gameMaker.players append totoNetId

     // gameMaker.owner shouldBe totoNetId

      //val titiNetId = NetPlayerId(joinerProbe.ref, titiId)

      gameMakerRef ! Join(titiId)

      //gameMaker.gameOwner shouldBe totoNetId

    }

    "given a join request answer AckJoin if there is remaining places" in {

      val joinerProbe = TestProbe()

      //val titiNetId = NetPlayerId(joinerProbe.ref, titiId)

      gameMakerRef ! Join(titiId)

      joinerProbe.expectMsg(100 millis, JoinAck(_))

      ()
    }

    "given a join request answer NackJoin if there is *NO* remaining places" in {
      val joinerProbe = TestProbe()

      //val titiNetId = NetPlayerId(joinerProbe.ref, titiId)

      gameMakerRef ! Join(titiId)

      joinerProbe.expectMsg(100 millis, JoinNack)

      ()
    }


    "given a joining request alert other players" in {

      val ownerProbe = TestProbe()
      val otherPlayerProbe = TestProbe()
      val joinerProbe = TestProbe()
      //val totoNetId= NetPlayerId(ownerProbe.ref, totoId)
      //gameMaker.players append totoNetId
      //val tutuNetId= NetPlayerId(otherPlayerProbe.ref, tutuId)
      //gameMaker.players append tutuNetId

      //val titiNetId = NetPlayerId(joinerProbe.ref, titiId)

      gameMakerRef ! Join(titiId)

      joinerProbe.expectMsg(100 millis, JoinAck)

      ownerProbe expectMsg (100 millis, Join(titiId))

      otherPlayerProbe expectMsg (100 millis, Join(titiId))

      ()
    }
  }
}
