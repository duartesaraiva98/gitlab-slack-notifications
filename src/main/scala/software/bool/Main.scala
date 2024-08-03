package software.bool

import software.bool.domain.{Destinees, Notification}
import software.bool.gitlab.GitlabApi
import software.bool.server.Server
import software.bool.slack.SlackApi
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {

  implicit val appRuntime: zio.Runtime[Any] = runtime

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Unit] =
    ZLayer.make[Unit](
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
