package gp.leval.codecs.json


import gp.leval.network.{GameDescription, GameRoomId}

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.*

implicit val gameDescriptionJsonDecoder: Decoder[GameDescription] = 
  deriveDecoder


implicit val gameDescriptionJsonEncoder: Encoder[GameDescription] = 
  deriveEncoder

implicit val gameRoomIdJsonDecoder: Decoder[GameRoomId] = 
  deriveDecoder


implicit val gameRoomIdJsonEncoder: Encoder[GameRoomId] = 
  deriveEncoder