package leval.core

/**
  * Created by lorilan on 6/21/16.
  */
trait Rules {
  def isValidBeeing(b : Being) : Boolean //Sinnlos != Antares
  def value(r : Rank) : Int // Variante de sinnlos. As = 11
  def canSoulBeSold : Boolean //variante du diable
  //variante Janus à 4 joueur
  //variante Nédémone, narrative
  //variante O'Stein, draft en début de partie

  //utiliser une decorateur pour implanter les variantes ?
}
