package gatling.upload

import java.io.File

trait Source

final case class Dir(get: File, recursive: Boolean) extends Source

final case class Files(get: List[File]) extends Source