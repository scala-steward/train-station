package es.eriktorr.train_station
package departure

import arrival.ExpectedTrains
import arrival.ExpectedTrains.ExpectedTrain
import event.Event.Departed
import station.Station
import station.Station.TravelDirection.Destination

import cats.Applicative
import cats.implicits._
import org.typelevel.log4cats.Logger

trait DepartureTracker[F[_]] {
  def save(event: Departed): F[Unit]
}

object DepartureTracker {
  def impl[F[_]: Applicative: Logger](
    station: Station[Destination],
    expectedTrains: ExpectedTrains[F]
  ): DepartureTracker[F] = (event: Departed) => {
    val updateExpectedTrains =
      expectedTrains.update(ExpectedTrain(event.trainId, event.from, event.expected)) *> F.info(
        s"${station.show} is expecting train ${event.trainId.show} from ${event.from.show} at ${event.expected.show}"
      )

    updateExpectedTrains.whenA(event.to === station)
  }
}
