package gatling.config

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.collection.JavaConverters._

object Config {
  val config: com.typesafe.config.Config = ConfigFactory.load()
  val maxResponseTime: Int = config.getInt("gatling.maxResponseTime")
  val successesPercent: Double = config.getDouble("gatling.successPercentile")
  val baseUrl: String = config.getString("general.baseUrl")
  val pauseDurationSecs: Int = config.getInt("general.pauseDurationSecs")
  val runDurationSecs: Int = config.getInt("general.runDurationSecs")
  val users: Int = config.getInt("general.users")
  val mode: String = config.getString("general.mode")

  val gatlingResultsDir: String = config.getString("gatling.resultsFolder")
  val awsRegion: String = config.getString("aws.region")
  val awsBucket: String = config.getString("aws.s3.bucket")
  val awsKey: String = config.getString("aws.s3.key")

  val httpConfig: HttpProtocolBuilder = http
    .baseURL(baseUrl)
    // Common headers
    .acceptHeader("""application/json,*/*""")
    .contentTypeHeader("application/json")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("""en-GB,en;q=0.8,en-US;q=0.6,bg;q=0.4""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:31.0) Gecko/20100101 Firefox/31.0""")
    .disableCaching

}