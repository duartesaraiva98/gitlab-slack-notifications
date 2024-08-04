package software.bool.domain

import zio.{TaskLayer, ZIO, ZLayer}

import java.io.File
import scala.io.Source

object GitlabIdSlackIdMap {

  def read(file: File): TaskLayer[GitlabIdSlackIdMap] = ZLayer.fromZIO {
    ZIO.scoped {
      for {
        source <- ZIO.acquireRelease(ZIO.attemptBlocking(Source.fromFile(file)))(s => ZIO.succeed(s.close()))
        lines <- ZIO.attempt(source.getLines())
        map = lines.flatMap(_.split(",") match {
          case Array(gitlabId, slackId) => Some(gitlabId.toInt -> slackId)
          case _ => None
        }).toSeq.toMap
        _ <- ZIO.logInfo(s"$map")
      } yield GitlabIdSlackIdMap(map)
    }
  }

}

class GitlabIdSlackIdMap(map: Map[Int, String]) {

  def get(slackId: Int): Option[String] = map.get(slackId)

}
