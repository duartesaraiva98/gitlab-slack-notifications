package software.bool.server

import software.bool.domain.Notification
import sttp.tapir.server.armeria.zio.ArmeriaZioServerInterpreter
import zio.{RLayer, ZIO, ZLayer}
import com.linecorp.armeria
import software.bool.slack.SlackApi

import java.util.concurrent.CompletableFuture

class Server()(using runtime: zio.Runtime[Any]) {

  val live: RLayer[Notification.Resolver & SlackApi, Unit] = ZLayer.scoped {
    for {
      resolver <- ZIO.service[Notification.Resolver]
      slackApi <- ZIO.service[SlackApi]
      server = new ServerLogic(resolver, slackApi)
      service = ArmeriaZioServerInterpreter().toService(server.gitlabLogic)
      serv = armeria.server.Server.builder().service(service).http(8081).build()
      _ <- acquireRelease(serv)
    } yield ()
  }
  
  private def acquireRelease(server: armeria.server.Server) =
    ZIO.acquireRelease(
      for {
        s <- ZIO.fromCompletableFuture(server.start().thenApply(_ => server))
        _ <- ZIO.logInfo(s"Started server at ${s.defaultHostname()}:${s.activePort()}")
      } yield s
    )(
      server => (
        for {
          _ <- ZIO.fromCompletableFuture(server.closeAsync().asInstanceOf[CompletableFuture[Unit]])
          _ <- ZIO.logInfo("Server stopped.")
        } yield ()
        ).orDie
    ).unit
}
