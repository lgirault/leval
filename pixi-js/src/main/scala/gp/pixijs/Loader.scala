package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.LoaderResource")
class Resource extends js.Object {

  val texture: Texture = js.native

  val textures: js.Dictionary[Texture] = js.native
}

@js.native
@JSGlobal("PIXI.Loader")
class Loader  extends js.Object {

  def add(url : String) : Loader = js.native
  def add(name:String, url : String) : Loader = js.native

  def load(cb : js.Function2[Loader, js.Dictionary[Resource], Unit]) : Unit = js.native

}



@js.native
@JSGlobal("PIXI.Loader")
object Loader extends js.Object {

  val shared : Loader = js.native

}