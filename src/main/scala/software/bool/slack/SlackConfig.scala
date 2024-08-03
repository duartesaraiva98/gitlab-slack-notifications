package software.bool.slack

import zio.config.*
import zio.Config.string
import zio.{Config, ConfigProvider, RLayer, ZIO, ZLayer}

case class SlackConfig(token: String)

object SlackConfig {
  
  private val config: Config[SlackConfig] = (string("SLACK_TOKEN").withDefault("test-token")).to[SlackConfig]
  
  val live: RLayer[ConfigProvider, SlackConfig] = ZLayer.fromZIO {
    for {
      provider <- ZIO.service[ConfigProvider]
      conf <- provider.load(config).orDie
    } yield conf
  }
}
