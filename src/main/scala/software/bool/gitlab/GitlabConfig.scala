package software.bool.gitlab

import zio.config.*
import zio.Config.string
import zio.{Config, ConfigProvider, RLayer, ZIO, ZLayer}

case class GitlabConfig(
                         url: String,
                         token: String,
                       )

object GitlabConfig {

  private val config: Config[GitlabConfig] = (string("GITLAB_URL").withDefault("http://localhost:9999") zip string("GITLAB_TOKEN").withDefault("fake tomen")).to[GitlabConfig]

  val live: RLayer[ConfigProvider, GitlabConfig] = ZLayer.fromZIO {
    for {
      provider <- ZIO.service[ConfigProvider]
      conf <- provider.load(config).orDie
    } yield conf
  }

}