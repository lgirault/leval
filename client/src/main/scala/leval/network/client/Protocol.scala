package leval.network.client

import akka.actor.ActorRef

case class SchedulerRef(actor : ActorRef)

case class BattleMapActorRef(actor : ActorRef)

case object EndTurn
case object BeginTurn