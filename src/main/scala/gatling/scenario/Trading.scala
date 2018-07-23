package gatling.scenario

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.{http, _}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.util.Random

object Trading {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val userTypes = Seq("buyer", "seller")
  val orderTypes = Seq("market", "sell")
  val random = new Random

  val orderParams: Iterator[Map[String, Int]] = Iterator.continually(
    // Random number will be accessible in session under variable "OrderRef"
    Map(
      "quantity" -> random.nextInt(100),
      "price" -> random.nextInt(10)
    )
  )

  def trade(runSecs: Int, pauseSecs: Int): ChainBuilder =
  // Loop for a period of time.
    feed(orderParams)
    .exec(session => session.set("usertype", userTypes(random.nextInt(userTypes.length))))
      .during(runSecs seconds) {
        doIf(session => session.attributes.get("usertype").contains("seller")) {
          doIfOrElse(_ => orderTypes(random.nextInt(orderTypes.length)) == "sell")(exec(
            http("""GET /api/v1/front/trading/limit/sell""")
              .get("""/api/v1/front/trading/limit/sell/${quantity}/${price}""")
              .header("X-Access-Token", """${access-token}""")
              .header("Client-Key", """${client-key}""")
              //Perform the required checks.
              .check(status.is(200),
              jsonPath("$.message").is("Sell Order added"))
          ))(exec(
            http("""GET /api/v1/front/trading/market/sell""")
              .get("""/api/v1/front/trading/market/sell/${quantity}""")
              .header("X-Access-Token", """${access-token}""")
              .header("Client-Key", """${client-key}""")
              //Perform the required checks.
              .check(status.is(200),
              jsonPath("$.message").is("Sell Order added"))
          ))
        }
          .doIf(session => session.attributes.get("usertype").contains("buyer")) {
            doIfOrElse(_ => orderTypes(random.nextInt(orderTypes.length)) == "sell")(exec(
              http("""GET /api/v1/front/trading/limit/buy""")
                .get("""/api/v1/front/trading/limit/buy/${quantity}/${price}""")
                .header("X-Access-Token", """${access-token}""")
                .header("Client-Key", """${client-key}""")
                //Perform the required checks.
                .check(status.is(200),
                jsonPath("$.message").is("Buy Order added"))
            ))(exec(
              http("""GET /api/v1/front/trading/market/buy""")
                .get("""/api/v1/front/trading/market/buy/${quantity}""")
                .header("X-Access-Token", """${access-token}""")
                .header("Client-Key", """${client-key}""")
                //Perform the required checks.
                .check(status.is(200),
                jsonPath("$.message").is("Buy Order added"))
            ))
          }
          // Pause between requests.
          .pause(pauseSecs seconds)
      }
}
