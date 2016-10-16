package leval

import akka.actor.Actor

/**
  * Created by lorilan on 8/27/16.
  */
package object network {
  def passive : Actor.Receive = {
    case _ => ()
  }

  type JoinAction = () => Unit
}
