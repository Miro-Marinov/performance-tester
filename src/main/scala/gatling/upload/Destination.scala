package gatling.upload

trait Destination

final case class S3(bucket: String, key: String) extends Destination
