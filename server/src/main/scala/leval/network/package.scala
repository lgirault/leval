package leval

import akka.actor.ActorRef
import leval.core.PlayerId

/**
  * Created by Loïc Girault on 31/08/16.
  */
package object network {
  type NetPlayerId = (ActorRef, PlayerId)

  implicit class NetPlayerIdOps( val nid : NetPlayerId) extends AnyVal {
    def actor = nid._1
    def id = nid._2
  }

}
