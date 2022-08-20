package gp.leval.routes

import gp.leval.components.*
import japgolly.scalajs.react.extra.router.{
  Resolution,
  RouterConfigDsl,
  RouterCtl,
  Redirect,
  BaseUrl,
  Router
}
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.vdom.html_<^.*

sealed trait AppPage

object AppPage {
  case object Home extends AppPage
  case object CreateGameForm extends AppPage
}

private val config = RouterConfigDsl[AppPage].buildConfig { dsl =>
  import dsl._
  // val itemRoutes: Rule =
  //   Item.routes.prefixPath_/("#items").pmap[AppPage](Items) {
  //     case Items(p) => p
  //   }
  (trimSlashes
    | staticRoute(root, AppPage.Home) ~> renderR(r =>
      HomeMenu(HomeMenu.Props(r))
    )
    | staticRoute("createGame", AppPage.CreateGameForm) ~> render(
      CreateGameForm(CreateGameForm.Text())
    ))
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
