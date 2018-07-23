package gatling.upload

import com.amazonaws.event.{ProgressEvent, ProgressListener}
import com.amazonaws.services.s3.transfer.Transfer
import org.slf4j.LoggerFactory

import scala.concurrent.{Future, Promise}

trait Result {
  def await(): Unit

  def progress(): Future[Unit]
}

final case class S3UploadResult(transfer: Transfer) extends Result {
  val logger = LoggerFactory.getLogger(getClass)


  def await(): Unit = {
    transfer.waitForCompletion()
    logger.info(s"\n${transfer.getDescription}:  ${transfer.getState}")
  }

  def progress(): Future[Unit] = {
    val p = Promise[Unit]()
    transfer.addProgressListener(new ProgressListener {
      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        import com.amazonaws.event.ProgressEventType
        progressEvent.getEventType match {
          case ProgressEventType.TRANSFER_FAILED_EVENT =>
            p.failure(transfer.waitForException)
            logger.info(s"Failed: ${transfer.getState}")
            transfer.removeProgressListener(this)
          case ProgressEventType.TRANSFER_COMPLETED_EVENT =>
            p.success({})
            logger.info(s"COMPLETED")
            transfer.removeProgressListener(this)
          case _ => logger.info(s"Transfer progress changed: ${transfer.getState}")
        }
      }
    })
    p.future
  }
}