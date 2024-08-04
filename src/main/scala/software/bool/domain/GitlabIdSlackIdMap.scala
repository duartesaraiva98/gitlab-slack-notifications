package software.bool.domain

import zio.config.*
import zio.Config.string
import zio.{ConfigProvider, RLayer, ZIO, ZLayer}

import scala.io.Source

object GitlabIdSlackIdMap {

  case class Config(filePath: String)

  object Config {
    private val config = string("STATIC_MAPPING_FILE").to[Config]

    val live: RLayer[ConfigProvider, Config] = ZLayer.fromZIO {
      for {
        provider <- ZIO.service[ConfigProvider]
        conf <- provider.load(config).orDie
      } yield conf
    }
  }

  val read: RLayer[Config, GitlabIdSlackIdMap] = ZLayer.fromZIO {
    ZIO.scoped {
      for {
        config <- ZIO.service[Config]
        source <- ZIO.acquireRelease(ZIO.attemptBlocking(Source.fromFile(config.filePath)))(s => ZIO.succeed(s.close()))
        lines <- ZIO.attempt(source.getLines())
        map = lines.flatMap(_.split(",") match {
          case Array(gitlabId, slackId) => Some(gitlabId.toInt -> slackId)
          case _ => None
        }).toSeq.toMap
        _ <- ZIO.logInfo(s"$map")
      } yield GitlabIdSlackIdMap(map)
    }
  }

  val live: ZLayer[ConfigProvider, Throwable, GitlabIdSlackIdMap] = Config.live >>> read
}

class GitlabIdSlackIdMap(map: Map[Int, String]) {

  def get(slackId: Int): Option[String] = map.get(slackId)

}
