package gp.leval.codecs

//import cats.implicits.*
import gp.leval.core.{Antares, AntaresHeliosCommon, CoreRules, Helios, Rules, Sinnlos}

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.*

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

  implicit val coreJsonRulesDecoder: Decoder[CoreRules] =
    Decoder.decodeString.emap { s =>
      coreRulesMap
        .get(s.toLowerCase)
        .toRight(
          s"Expected one of ${coreRulesMap.keys.mkString("'", "','", "'")} but got ${s}"
        )
    }

  implicit val rulesJsonDecoder: Decoder[Rules] = deriveDecoder[Rules]

  implicit val coreJsonRulesEncoder: Encoder[CoreRules] =
    Encoder.instance[CoreRules] {
      case Antares => Json.fromString("antares")
      case Sinnlos => Json.fromString("sinnlos")
      case Helios  => Json.fromString("helios")
    }

  implicit val rulesJsonEncoder: Encoder[Rules] = deriveEncoder[Rules]

}
