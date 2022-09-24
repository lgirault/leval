package gp.leval.core


import scala.collection.mutable

/**
  * Created by Loïc Girault on 01/07/16.
  */

trait GameObserver {
  def notify[A](m : Move[A], res : A) : Unit
}

class ObservableGame(g : Game) extends MutableGame(g){


  val observers : mutable.ListBuffer[GameObserver] = new mutable.ListBuffer[GameObserver]

  def notifyAll[A](ma: Move[A], res : A) : Unit =
    observers.foreach(_.notify(ma,res))

  override def apply[A](ma: Move[A]): A /*Id[A]*/ = {
    val res = super.apply(ma)
    notifyAll(ma, res)
    res
  }

}
