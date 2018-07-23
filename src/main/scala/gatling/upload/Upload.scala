package gatling.upload

import java.io.File

import com.amazonaws.services.s3.transfer.TransferManager

import scala.collection.JavaConverters._


final case class Upload[Dest <: Destination](run: (Source, Dest) => Result)

object Upload {
  def toS3(txManager: TransferManager): Upload[S3] = {
    new Upload(run = (source, dest) => source match {
      case source: Dir =>
        val transfer = txManager.uploadDirectory(dest.bucket, dest.key, source.get, source.recursive)
        S3UploadResult(transfer)

      case source: Files =>
        val transfer = txManager.uploadFileList(dest.bucket, dest.key, new File("."), source.get.asJava)
        S3UploadResult(transfer)

      case _ => throw new UnsupportedOperationException()
    })
  }
}
