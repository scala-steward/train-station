package es.eriktorr.train_station

import TrainControlPanelConfig.HttpServerConfig
import station.Station
import station.Station.TravelDirection.{Destination, Origin}

import cats.data.NonEmptyList
import cats.effect.IO
import eu.timepit.refined.api.Refined
import weaver._

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
object TrainControlPanelConfigSuite extends SimpleIOSuite {
  test("load config params from env vars") {
    TrainControlPanelConfig
      .load[IO]
      .map(actualConfig =>
        expect(
          actualConfig == TrainControlPanelConfig(
            HttpServerConfig(Refined.unsafeApply("0.0.0.0"), Refined.unsafeApply(8080)),
            Station.fromString[Origin]("Barcelona").toOption.get,
            NonEmptyList.fromListUnsafe(
              List(
                Station.fromString[Destination]("Madrid").toOption.get,
                Station.fromString[Destination]("Valencia").toOption.get
              )
            )
          )
        )
      )
  }
}
