package gatling.simulation

import gatling.config.Config._
import gatling.scenario.{SetUp, Trading}
import io.gatling.core.Predef._

class TradingSimulation extends Simulation {

  setUp(
    scenario("Trading")
      .exec(SetUp.newUser)
      .exec(Trading.trade(runDurationSecs, pauseDurationSecs))
      .inject(atOnceUsers(users))
  )
    .protocols(httpConfig)
    .assertions(
      global.responseTime.max.lt(maxResponseTime),
      global.successfulRequests.percent.gt(successesPercent)
    )
}
