package gp.leval.components

import monocle.syntax.all.*
import gp.leval.core.{Antares, CoreRules, Helios, PlayerId, Rules, Sinnlos}
import gp.leval.network.{GameDescription, GameRoomId}
import gp.leval.codecs.json.{gameDescriptionJsonEncoder, gameRoomIdJsonDecoder}
import gp.leval.webservice.WebServiceClient
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.extra.Ajax
import io.circe.syntax.*
import japgolly.scalajs.react.extra.router.RouterCtl
import gp.leval.routes.AppPage

import scala.util.{Failure, Success}

object CreateGameForm:

  // controlled component pattern for forms:
  // https://reactjs.org/docs/forms.html#controlled-components
  // add private/public game option
  type State = GameDescription
  val State = GameDescription

  case class Props(
      ctrl: RouterCtl[AppPage.GameRoom],
      text: Text = Text()
  )

  case class Text(
      createGameFormTitle: String = "Créer une partie",
      createSubmit: String = "Créer"
  )

  // https://japgolly.github.io/scalajs-react/#examples/ajax-1
  // or maybe directly open a websocket:
  // https://japgolly.github.io/scalajs-react/#examples/websockets
  // https://scala-js.github.io/scala-js-dom/#dom.Websocket

  private val coreRulesList =
    CoreRules.values

  class Backend($ : BackendScope[Props, State]) {

    def onPlayerNameChange(e: ReactEventFromInput): Callback = {
      val name = e.target.value
      $.modState(_.focus(_.owner.name).replace(name))
    }
    def onCoreRulesChange(
        e: ReactEventFromInput
    ): Callback =
      e.preventDefaultCB >>
        $.modState(
          _.focus(_.rules.coreRules)
            .replace(CoreRules(e.target.value))
        )

    def handleSubmit(
        e: ReactEventFromInput
    ): Callback =
      for {
        _ <- e.preventDefaultCB  
        ctrl <- $.props.map(_.ctrl)
        gameDescription <- $.state 
        _ <- WebServiceClient
          .post[GameRoomId](
            "createGame",
            Some(gameDescription.asJson.noSpaces)
          )
          .completeWith {
            case Success(GameRoomId(roomId)) =>
              ctrl.set(AppPage.GameRoom(roomId))
            case Failure(e) => 
              Callback.throwException(e)
          }
          //_ <- CallbackTo[Unit](println("foo")) 
      } yield ()

    def render(P: Props, state: State) = {
      val txt = P.text
      <.div(
        <.h1(txt.createGameFormTitle),
        <.form(
          ^.onSubmit ==> handleSubmit,
          <.input(
            ^.value := state.owner.name,
            ^.onChange ==> onPlayerNameChange
          ),
          <.select(
            ^.value := state.rules.coreRules.id,
            ^.onChange ==> onCoreRulesChange,
            coreRulesList.toTagMod(r =>
              <.option(
                ^.value := r.id,
                r.toString
              )
            )
          ),
          <.button(txt.createSubmit)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("HomeMenu")
    .initialState(State(PlayerId(None, ""), Rules(Sinnlos)))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
