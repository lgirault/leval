package gp.leval.codecs.entities

import cats.effect.{Concurrent, Sync}
import cats.implicits.*
import gp.leval.network.{GameDescription, GameRoomId}
import gp.leval.core.{
  Antares,
  AntaresHeliosCommon,
  CoreRules,
  PlayerId,
  Helios,
  Rules,
  Sinnlos
}
import org.http4s.{
  EntityEncoder,
  EntityDecoder,
  FormDataDecoder,
  HttpRoutes,
  ParseFailure,
  QueryParamDecoder
}
import org.http4s.FormDataDecoder.*
import gp.leval.codecs.json.*

import org.http4s.circe.*

private val coreRulesMap: Map[String, CoreRules] =
  Map(
    "antares"
      -> Antares,
    "helios"
      -> Helios,
    "sinnlos"
      -> Sinnlos
  )

implicit val coreRulesDecoder: QueryParamDecoder[CoreRules] =
  QueryParamDecoder[String].emap[CoreRules](param =>
    coreRulesMap.get(param.toLowerCase).toRight {
      val msg =
        s"Expected one of ${coreRulesMap.keys.mkString("'", "','", "'")} but got ${param}"
      ParseFailure(msg, msg)
    }
  )

implicit val rulesMapper: FormDataDecoder[Rules] = (
  field[CoreRules]("coreRules"),
  field[Boolean]("ostein"),
  field[Boolean]("allowMulligan"),
  field[Boolean]("nedemone"),
  field[Boolean]("janus")
).mapN(Rules.apply)

implicit def rulesEntityEncoder[F[_]]: EntityEncoder[F, Rules] =
  jsonEncoderOf

implicit def gameDescriptionEntityEncoder[F[_]]
    : EntityEncoder[F, GameDescription] =
  jsonEncoderOf

implicit def gameDescriptionEntityDecoder[F[_]: Concurrent]
    : EntityDecoder[F, GameDescription] =
  jsonOf

implicit def playerIdEntityEncoder[F[_]]
    : EntityEncoder[F, PlayerId] =
  jsonEncoderOf

implicit def playerIdEntityDecoder[F[_]: Concurrent]
    : EntityDecoder[F, PlayerId] =
  jsonOf


implicit def gameRoomIdEntityEncoder[F[_]]: EntityEncoder[F, GameRoomId] = 
  jsonEncoderOf


implicit def gameRoomIdEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, GameRoomId] = 
  jsonOf