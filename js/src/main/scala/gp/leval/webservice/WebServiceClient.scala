package gp.leval.webservice

import gp.leval.webservice.WebServiceClient.paramsToQueryString
import io.circe.*
import io.circe.parser.*
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.extra.router.BaseUrl
import org.scalajs.dom.XMLHttpRequest

import scala.scalajs.js
import scala.scalajs.js.URIUtils.encodeURIComponent

class WebServiceClient:

  def request(
      method: String,
      endPoint: String,
      body: Option[js.Any] = None,
      params: Map[String, String] = Map.empty,
      protectedRoute: Boolean = false
  ): AsyncCallback[XMLHttpRequest] =

    val url =
      s"http://localhost:8080/$endPoint${paramsToQueryString(params)}"
      //s"${BaseUrl.fromWindowOrigin}/$endPoint${paramsToQueryString(params)}"

    if protectedRoute /*&& Keycloak.token.isEmpty*/ then
      AsyncCallback.throwException(
        new Exception(
          s"Error. Authenticated without token, endpoint:${endPoint}"
        )
      )
    else
      val request = Ajax
        .apply(method, url)
        .setRequestContentTypeJson
        .setRequestHeader("Accept", "application/json")

      body.fold(request.send)(request.send).asAsyncCallback

  def post[T: Decoder](
      endPoint: String,
      body: Option[js.Any] = None,
      params: Map[String, String] = Map.empty,
      protectedRoute: Boolean = false
  ): AsyncCallback[T] =
    request("POST", endPoint, body, params, protectedRoute)
      .flatMap(xhr =>
        decode[T](xhr.responseText).fold(
          AsyncCallback.throwException(_),
          AsyncCallback.pure
        )
      )

object WebServiceClient extends WebServiceClient:

  def paramsToQueryString(params: Map[String, String]): String =
    params
      .map { case (k, v) =>
        s"${encodeURIComponent(k)}=${encodeURIComponent(v)}"
      }
      .mkString("?", "&", "")
