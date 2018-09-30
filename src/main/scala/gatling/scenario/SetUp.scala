package gatling.scenario

import java.util.UUID

import io.gatling.core.Predef.{exec, _}
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.{http, status, _}
import org.slf4j.{Logger, LoggerFactory}

object SetUp {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  var signupPath = "/api/front/v1/signup"
  var activatePath = "/api/front/v1/activate-account"
  var signinPath = "/api/front/v1/login"
  val commonHeaders = Map(
    "Content-Type" -> """application/json"""
  )

  val userNames: Iterator[Map[String, String]] = {
    val seq: Iterator[Int] = Iterator.from(1)

    Iterator.continually(
      // Random number will be accessible in session under variable "OrderRef"
      Map("userName" -> s"finraxUser${seq.next()}")
    )
  }

  val registerAndActivateScenario: ScenarioBuilder = scenario("Register & Activate")
    .feed(userNames)
    .exec(signup)
//    .exec(activate) // This is done via e-mail now

  val signInScenario: ScenarioBuilder = scenario("Sign-In")
    .feed(userNames)
    .exec(signIn)

  private lazy val signup: ChainBuilder =
    exec(
      http(s"POST $signupPath")
        .post(s"$signupPath")
        .body(StringBody("""{"email":"${userName}@finrax.com","password":"123456"}""")).asJSON
        .disableFollowRedirect
        .headers(commonHeaders)
        .check(
          status.is(200)
//          jsonPath("$.data.user.verification_token").saveAs("verification-token")
        )
    )

  private lazy val activate = exec(http(s"GET $activatePath")
    .get(s"$activatePath")
    .queryParam("token", """${verification-token}""")
    .disableFollowRedirect
    .headers(commonHeaders)
    .header("X-Access-Token", """${access-token}""")
    .check(
      status.is(200)
    ))

  private lazy val signIn = exec(
    http(s"POST $signinPath")
      .post(s"$signinPath")
      .body(StringBody("""{"email":"${userName}@finrax.com","password":"123456"}""")).asJSON
      .disableFollowRedirect
      .headers(commonHeaders)
      .check(
        status.is(200),
        jsonPath("$.data.token").saveAs("access-token"),
        jsonPath("$.data.user.client_key").saveAs("client-key")
      )
  )
}
