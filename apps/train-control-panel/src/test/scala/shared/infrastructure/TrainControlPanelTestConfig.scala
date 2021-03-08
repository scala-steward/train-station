package es.eriktorr.train_station
package shared.infrastructure

import TrainControlPanelConfig.{HttpServerConfig, JdbcConfig, KafkaConfig}
import station.Station
import station.Station.TravelDirection.{Destination, Origin}

import cats.data.NonEmptyList
import ciris.Secret
import eu.timepit.refined.api.Refined
import eu.timepit.refined.cats._

object TrainControlPanelTestConfig {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  val testConfig: TrainControlPanelConfig = TrainControlPanelConfig(
    HttpServerConfig(Refined.unsafeApply("0.0.0.0"), Refined.unsafeApply(8080)),
    JdbcConfig(
      Refined.unsafeApply("org.postgresql.Driver"),
      Refined.unsafeApply("jdbc:postgresql://localhost:5432/train_station"),
      Refined.unsafeApply("train_station"),
      Secret(Refined.unsafeApply("changeme"))
    ),
    KafkaConfig(
      NonEmptyList.one(Refined.unsafeApply("localhost:29092")),
      Refined.unsafeApply("train-station"),
      Refined.unsafeApply("train-arrivals-and-departures"),
      Refined.unsafeApply("http://localhost:8081/api/ccompat")
    ),
    Station.fromString[Origin]("Barcelona").toOption.get,
    NonEmptyList.fromListUnsafe(
      List(
        Station.fromString[Destination]("Madrid").toOption.get,
        Station.fromString[Destination]("Valencia").toOption.get
      )
    )
  )
}