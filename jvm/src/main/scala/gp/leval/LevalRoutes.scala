package gp.leval

import cats.effect.{Concurrent, Sync}
import cats.implicits.*
import leval.core.{Antares, CoreRules, Helios, Rules, Sinnlos}
import org.http4s.{FormDataDecoder, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.FormDataDecoder.*
import org.http4s.dsl.Http4sDsl

object LevalRoutes {
  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
      for {
        greeting <- H.hello(HelloWorld.Name(name))
        resp <- Ok(greeting)
      } yield resp
    }
  }

  def gameRoutes[F[_]: Concurrent](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import RulesCodecs._
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "createGame" =>
      for {
        rules <- req.as[Rules]
        // greeting <- H.hello(HelloWorld.Name(name))
        resp <- Ok(rules)
      } yield resp
    }
  }
}

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

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
