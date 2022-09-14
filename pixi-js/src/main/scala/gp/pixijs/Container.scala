package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("PIXI.Container")
class Container extends DisplayObject {
  def addChild(children: DisplayObject*) : DisplayObject = js.native

  val children : js.Array[DisplayObject] = js.native

  def removeChildren(beginIndex: Int = 0,
                     endIndex:Int = this.children.length):js.Array[DisplayObject] = js.native

  var width: Double = js.native
  var height: Double = js.native
  var sortableChildren: Boolean = js.native


}

object Container:
  def apply(setUp: Container => Unit) : Container = 
    val c = new Container
    setUp(c)
    c
