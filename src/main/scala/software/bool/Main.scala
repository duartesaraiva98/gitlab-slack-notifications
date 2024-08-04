package software.bool

import com.slack.api.Slack
import software.bool.domain.{Destinees, GitlabIdSlackIdMap, Notification}
import software.bool.gitlab.{GitlabApi, GitlabConfig}
import software.bool.server.Server
import software.bool.slack.{SlackApi, SlackConfig}
import sttp.client3.armeria.zio.ArmeriaZioBackend
import zio.{ConfigProvider, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {

  implicit val appRuntime: zio.Runtime[Any] = runtime

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Unit] =
    ZLayer.make[Unit](
      ZLayer.succeed(ConfigProvider.defaultProvider),
      GitlabIdSlackIdMap.live,
      Destinees.DestineesConfig.live,
      GitlabConfig.live,
      SlackConfig.live,
      ZLayer.succeed(Slack.getInstance()),
      ArmeriaZioBackend.layer(),
      SlackApi.live,
      GitlabApi.live,
      Destinees.live,
      Notification.live,
      new Server().live,
    )

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    ZIO.logInfo("Bootstrap completed")
      *> ZIO.unit.forever
}
