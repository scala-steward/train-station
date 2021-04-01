package es.eriktorr.train_station
package http.infrastructure

import TrainControlPanelConfig.HttpServerConfig
import arrival.Arrivals
import arrival.syntax._
import departure.Departures
import departure.syntax._

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Timer}
import cats.implicits._
import fs2.Stream
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.http4s.common.Http4sRequestFilter
import io.janstenpickle.trace4cats.http4s.server.syntax._
import io.janstenpickle.trace4cats.inject.{EntryPoint, Trace}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, GZip, Logger => Http4sLogger}

import scala.concurrent.ExecutionContext

object HttpServer {
  def stream[F[_]: ConcurrentEffect: Timer: Trace](
    arrivals: Arrivals[F],
    departures: Departures[F],
    entryPoint: EntryPoint[F],
    httpServerConfig: HttpServerConfig,
    executionContext: ExecutionContext
  ): Stream[F, Nothing] = {

    val httpApp = B3Propagation
      .make[F, Kleisli[F, Span[F], *]](
        AllHttpRoutes.routes[Kleisli[F, Span[F], *]](
          arrivals.liftTrace[Kleisli[F, Span[F], *]]
        ) <+> AllHttpRoutes.routes[Kleisli[F, Span[F], *]](
          departures.liftTrace[Kleisli[F, Span[F], *]]
        )
      )
      .inject(entryPoint, requestFilter = Http4sRequestFilter.kubernetesPrometheus)
      .orNotFound

    val finalHttpApp = Http4sLogger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[F](executionContext)
      .bindHttp(httpServerConfig.port.value, httpServerConfig.host.value)
      .withHttpApp(CORS(GZip(finalHttpApp)))
      .serve
  }.drain
}
