package es.eriktorr.train_station
package http.infrastructure

import arrival.Arrivals
import arrival.Arrivals.{Arrival, ArrivalError}
import departure.Departures
import departure.Departures.Departure
import departure.Departures.DepartureError.UnexpectedDestination
import json.infrastructure.EventJsonProtocol

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

object AllHttpRoutes extends EventJsonProtocol {
  def arrivalRoutes[F[_]: Sync](A: Arrivals[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ POST -> Root / "arrival" =>
      req
        .as[Arrival]
        .flatMap { arrival =>
          A.register(arrival).flatMap(arrivalEvent => Created(arrivalEvent.id))
        }
        .recoverWith { case ArrivalError.UnexpectedTrain(trainId) =>
          BadRequest(show"Unexpected train $trainId")
        }
    }
  }

  def departureRoutes[F[_]: Sync](D: Departures[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ POST -> Root / "departure" =>
      req
        .as[Departure]
        .flatMap { departure =>
          D.register(departure).flatMap(departureEvent => Created(departureEvent.id))
        }
        .recoverWith { case UnexpectedDestination(destination) =>
          BadRequest(show"Unexpected destination $destination")
        }
    }
  }
}
