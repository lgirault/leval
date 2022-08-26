package gp.leval

import cats.effect.{Async, Ref, Sync}
import cats.effect.std.Queue
import cats.implicits.*
import monocle.syntax.all.*
import fs2.Stream

import gp.leval.codecs.entities.*
import gp.leval.core.{Antares, CoreRules, Helios, PlayerId, Rules, Sinnlos}
import gp.leval.network.{ServerToClientMessage, GameDescription, GameRoomId}

import org.http4s.dsl.Http4sDsl
import org.http4s.{FormDataDecoder, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.FormDataDecoder.*
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

import java.util.UUID

object LevalRoutes {

  type InputMessage = String
  type OutputMessage = String

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
      for {
        greeting <- H.hello(HelloWorld.Name(name))
        resp <- Ok(greeting)
      } yield resp
    }
  }

  def gameRoutes[F[_]](
      state: Ref[F, GameServerState[F]]
  )(
      webSocketBuilder: WebSocketBuilder[F]
  )(implicit F: Async[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] {
      case req @ POST -> Root / "createGame" =>
        for {
          gameDesc <- req.as[GameDescription]
          room <- F.delay {
            GameRoom[F](UUID.randomUUID(), gameDesc.rules, gameDesc.owner, Nil)

          }
          _ <- state.update(_.focus(_.rooms).modify(_ + (room.id -> room)))
          resp <- Ok(GameRoomId(room.id))
        } yield resp

      case req @ POST -> Root / "joinGame" / roomId =>
        def processInput(
            wsfStream: Stream[F, WebSocketFrame]
        ): Stream[F, Unit] = {
          val parsedWebSocketInput: Stream[F, InputMessage] =
            wsfStream
              .collect {
                case Text(text, _) => ??? // InputMessage.parse(userName, text)

                // Convert the terminal WebSocket event to a User disconnect message
                case Close(_) => ??? // /Disconnect(userName)
              }

          ???
        }

        for {

          playerId <- req.as[PlayerId]

          output <- Queue.bounded[F, ServerToClientMessage](10)

          toClient = Stream
            .fromQueueUnterminated(output, 10)
            .map(msg => Text(msg.toString))

          roomUUID <- F.delay(UUID.fromString(roomId))  

          _ <- state.update(
            _.focus(_.rooms.index(roomUUID).players)
              .modify((playerId -> output) :: _)
          )

          response <- webSocketBuilder.build(toClient, processInput)
        } yield response

    }
  }
}

case class GameServerState[F[_]](rooms: Map[UUID, GameRoom[F]])

case class GameRoom[F[_]](
    id: UUID,
    rules: Rules,
    owner: PlayerId,
    players: List[(PlayerId, Queue[F, ServerToClientMessage])]
)

import cats.Applicative
import cats.implicits.*
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.*

trait HelloWorld[F[_]] {
  def hello(n: HelloWorld.Name): F[HelloWorld.Greeting]
}

object HelloWorld {
  implicit def apply[F[_]](implicit ev: HelloWorld[F]): HelloWorld[F] = ev

  final case class Name(name: String) extends AnyVal

  /** More generally you will want to decouple your edge representations from
    * your internal data structures, however this shows how you can create
    * encoders for your data.
    */
  final case class Greeting(greeting: String) extends AnyVal
  object Greeting {
    implicit val greetingEncoder: Encoder[Greeting] = new Encoder[Greeting] {
      final def apply(a: Greeting): Json = Json.obj(
        ("message", Json.fromString(a.greeting))
      )
    }
    implicit def greetingEntityEncoder[F[_]]: EntityEncoder[F, Greeting] =
      jsonEncoderOf[Greeting]
  }

  def impl[F[_]: Applicative]: HelloWorld[F] = new HelloWorld[F] {
    def hello(n: HelloWorld.Name): F[HelloWorld.Greeting] =
      Greeting("Hello, " + n.name).pure[F]
  }
}
