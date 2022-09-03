package gp.leval

import cats.effect.{Async, Resource, Ref}
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.{Logger, CORS}
import org.http4s.server.websocket.WebSocketBuilder

object LevalServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      helloWorldAlg = HelloWorld.impl[F]
      // jokeAlg = Jokes.impl[F](client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.

      state <- Stream.eval(
        Ref.of[F, GameServerState[F]](GameServerState(Map.empty))
      )

      httpApp = (wsb: WebSocketBuilder[F]) =>
        (
          LevalRoutes
            .helloWorldRoutes[F](helloWorldAlg) <+>
            LevalRoutes.gameRoutes[F](state)(wsb)
            // Hellohttp4sRoutes.jokeRoutes[F](jokeAlg)
        ).orNotFound

      // With Middlewares in place
      finalHttpApp = (wsb: WebSocketBuilder[F]) =>
        CORS.policy.withAllowOriginAll(Logger.httpApp(true, true)(httpApp(wsb)))

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpWebSocketApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
