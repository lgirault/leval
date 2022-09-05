package gp.leval.routes

import gp.leval.components.*
import japgolly.scalajs.react.extra.router.{
  BaseUrl,
  Redirect,
  Resolution,
  Router,
  RouterConfigDsl,
  RouterCtl
}
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.vdom.html_<^.*

import java.util.UUID

sealed trait AppPage

object AppPage {
  case object Home extends AppPage
  case object CreateGameForm extends AppPage

  case class GameRoom(id: UUID) extends AppPage
}

private val config = RouterConfigDsl[AppPage].buildConfig { dsl =>
  import dsl.*
  // val itemRoutes: Rule =
  //   Item.routes.prefixPath_/("#items").pmap[AppPage](Items) {
  //     case Items(p) => p
  //   }
  (trimSlashes
    | staticRoute(root, AppPage.Home) ~> renderR(routerCtl =>
      HomeMenu(HomeMenu.Props(routerCtl))
    )
    | staticRoute("#createGame", AppPage.CreateGameForm) ~> renderR(routerCtl =>
      CreateGameForm(CreateGameForm.Props(routerCtl.narrow[AppPage.GameRoom]))
    )
    | dynamicRouteCT(
      "#gameRoom" / uuid.caseClass[AppPage.GameRoom]
    ) ~> dynRender(gameRoomPage => GameRoom(gameRoomPage.id)))
    .notFound(redirectToPage(AppPage.Home)(HistoryReplace))
    .renderWith(layout)
}

private def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) =
  <.div(
    // <.header(<.h1("foobar")),
    // TopNav(TopNav.Props(mainMenu, r.page, c)),
    r.render() // ,
    // Footer()
  )

private val baseUrl = BaseUrl.fromWindowOrigin / "index.html"

//doc :
//https://github.com/japgolly/scalajs-react/blob/master/doc/ROUTER.md#features
val AppRouter = Router(baseUrl, config)
