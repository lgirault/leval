package gp.leval

import cats.effect.{Concurrent, Sync}
import cats.implicits.*
import leval.core.{
  Antares,
  AntaresHeliosCommon,
  CoreRules,
  Helios,
  Rules,
  Sinnlos
}
import org.http4s.{
  EntityEncoder,
  FormDataDecoder,
  HttpRoutes,
  ParseFailure,
  QueryParamDecoder
}
import org.http4s.FormDataDecoder.*
import org.http4s.dsl.Http4sDsl
import io.circe.{Json, Decoder, Encoder}
import io.circe.generic.semiauto.*
//import org.http4s._
//import org.http4s.implicits._
//import org.http4s.client.Client
//import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
//import org.http4s.Method._

object RulesCodecs {
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

  implicit val coreJsonRulesEncoder: Encoder[CoreRules] =
    Encoder.instance[CoreRules] {
      case Antares => Json.fromString("antares")
      case Sinnlos => Json.fromString("sinnlos")
      case Helios  => Json.fromString("helios")
    }

  implicit val rulesJsonEncoder: Encoder[Rules] = deriveEncoder[Rules]

  implicit def rulesEntityEncoder[F[_]]: EntityEncoder[F, Rules] =
    jsonEncoderOf

}
