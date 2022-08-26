package gp.leval.codecs.json

import gp.leval.core.PlayerId
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.*

implicit val playerIdDecoder: Decoder[PlayerId] =
  deriveDecoder[PlayerId]

implicit val playerIdEncoder: Encoder[PlayerId] =
  deriveEncoder[PlayerId]
