package leval
package network
package client




object BeforeWaitingRoom{
  type MaxPlayer = Int
  type CurrentNumPlayer = Int
}

object GameListView {
  type JoinAction = () => Unit
}

sealed abstract class UserMapRelationship
case object Owner extends UserMapRelationship
case object Joiner extends UserMapRelationship

