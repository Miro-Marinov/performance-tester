package gatling.scenario

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.{http, _}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.util.Random

object Trading {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val userTypes = Array("buyer", "seller")
  val orderTypes = Array("market",  "limit")
  val pairs = Array("ETH-BTC", "XRP-BTC", "LTC-BTC", "LTC-ETH", "BCH-ETH", "BTC-EUR", "ETH-EUR")
  val random = new Random

  val orderParams: Iterator[Map[String, Any]] = Iterator.continually(
    // Random number will be accessible in session under variable "OrderRef"
    Map(
      "quantity" -> random.nextDouble() * 100,
      "price" -> random.nextDouble() * 10,
      "pair" -> pairs(random.nextInt(pairs.length))
    )
  )

  def trade(runSecs: Int, pauseSecs: Int): ChainBuilder =
  // Loop for a period of time.
    feed(orderParams)
      .exec(session => session.set("usertype", userTypes(random.nextInt(userTypes.length))))
      .during(runSecs seconds) {
        doIf(session => session.attributes.get("usertype").contains("seller")) {
          doIfOrElse(_ => orderTypes(random.nextInt(orderTypes.length)) == "limit")(exec(
            http("""POST /api/v1/front/trading/limit/sell""")
              .post("""/api/v1/front/trading/limit/sell""")
              .body(StringBody(
                """{
                                 "orderQty": "${quantity}",
                                 "limitPrice": "${price}",
                                 "orderType": "Limit",
                                 "side": "Sell",
                                 "symbol": "${pair}",
                                 "text": "Submission of Limit order"
                                 }""")).asJSON
              .header("X-Access-Token", """${access-token}""")
              .header("Client-Key", """${client-key}""")
              //Perform the required checks.
              .check(status.is(200))
          ))(exec(
            http("""POST /api/v1/front/trading/market/sell""")
              .post("""/api/v1/front/trading/market/sell""")
              .body(StringBody(
                """{
                                   "orderQty": "${quantity}",
                                   "orderType": "Market",
                                   "side": "Sell",
                                   "symbol": "${pair}",
                                   "text": "Submission of Market order"
                                 }
                                 """)).asJSON
              .header("X-Access-Token", """${access-token}""")
              .header("Client-Key", """${client-key}""")
              //Perform the required checks.
              .check(status.is(200))
          ))
        }
          .doIf(session => session.attributes.get("usertype").contains("buyer")) {
            doIfOrElse(_ => orderTypes(random.nextInt(orderTypes.length)) == "limit")(exec(
              http("""POST /api/v1/front/trading/limit/buy""")
                .post("""/api/v1/front/trading/limit/buy""")
                .body(StringBody(
                  """{
                                     "orderQty": "${quantity}",
                                     "limitPrice": "${price}",
                                     "orderType": "Limit",
                                     "side": "Buy",
                                     "symbol": "${pair}",
                                     "text": "Submission of Limit order"
                                   }
                                   """)).asJSON
                .header("X-Access-Token", """${access-token}""")
                .header("Client-Key", """${client-key}""")
                //Perform the required checks.
                .check(status.is(200))
            ))(exec(
              http("""POST /api/v1/front/trading/market/buy""")
                .post("""/api/v1/front/trading/market/buy""")
                .body(StringBody(
                  """{
                                     "orderQty": "${quantity}",
                                     "orderType": "Market",
                                     "side": "Buy",
                                     "symbol": "${pair}",
                                     "text": "Submission of Market order"
                                   }
                                   """)).asJSON
                .header("X-Access-Token", """${access-token}""")
                .header("Client-Key", """${client-key}""")
                //Perform the required checks.
                .check(status.is(200))
            ))
          }
          // Pause between requests.
          .pause(pauseSecs seconds)
      }
}
