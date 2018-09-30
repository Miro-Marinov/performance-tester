package gatling

import java.io.File

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.typesafe.config.ConfigFactory
import gatling.config.Config
import gatling.upload.{Dir, S3, Upload}
import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

object Main extends App {
  val logger = LoggerFactory.getLogger(getClass)
  val gatlingResultsDir = ConfigFactory.load().getString("gatling.resultsFolder")
  val runMode = ConfigFactory.load().getString("general.mode")

  def runGatling(): Unit = {
    val props = new GatlingPropertiesBuilder
    runMode match {
      case "trading" => props.simulationClass("gatling.simulation.TradingSimulation")
      case _ => props.simulationClass("gatling.simulation.SetupSimulation")
    }

    props.resultsDirectory(gatlingResultsDir)
    // Run the gatling simulation.
    Gatling.fromMap(props.build)
  }

  /**
    * The default transfer manager is looking for AWS credentials in env vars, system properties or machine's IAM role.
    *
    * @see DefaultAWSCredentialsProviderChain
    */
  def uploadToS3(): Unit = {
    val client = AmazonS3Client.builder
      .withRegion(Config.awsRegion)
      // Allows the client to access objects in a different from the default region.
      .withForceGlobalBucketAccessEnabled(true)
      .build
    val txManager = TransferManagerBuilder.standard().withS3Client(client).build()
    val bucket: String = Config.awsBucket
    val key: String = Config.awsKey

    // Upload the resulting report to AWS S3:
    val latestResultDir = new File(Config.gatlingResultsDir).listFiles().filter(_.isDirectory).maxBy(_.getName)
    logger.info(s"Uploading to s3 bucket: $bucket/$key/frontbench")
    val transfer = Upload.toS3(txManager).run(Dir(latestResultDir, recursive = true), S3(bucket, key + "/frontbench"))

    import scala.concurrent.ExecutionContext.Implicits.global
    transfer.progress() onComplete {
      case Success(_) => logger.info("Upload completed successfully!")
      case Failure(e) => logger.error(s"Upload failed: ${e.printStackTrace()}")
    }
  }

  runGatling()
}