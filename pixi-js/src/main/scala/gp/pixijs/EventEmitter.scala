package gp.pixijs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait Event[T] extends js.Any {
  val target: T = js.native
}

@js.native
@JSGlobal("PIXI.utils.EventEmitter")
class EventEmitter extends js.Object {

  def on[T](event: String,
            listener: js.Function1[Event[T], Unit]): Unit = js.native

  def on[T, Context](event: String,
                     listener: js.ThisFunction1[Context, Event[T], Unit],
                     context: Context): Unit = js.native

}
