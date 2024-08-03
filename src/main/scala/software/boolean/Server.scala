package software.boolean

import com.linecorp.armeria
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import sttp.tapir.*
import zio.{RIO, RLayer, Scope, Task, TaskLayer, ULayer, ZIO, ZLayer}
import io.circe.parser.decode
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.server.armeria.zio.ArmeriaZioServerInterpreter

import java.util.concurrent.CompletableFuture

object Server {
  def apply()(using r: zio.Runtime[Any]) = new Server
}

class Server(using r: zio.Runtime[Any]) {
  private val gitlabLogic: ZServerEndpoint[Any, Any] =
    Endpoints
      .gitlab
      .zServerLogic { (header, jsonBody) =>
        header.toLowerCase match
          case "note hook" =>
            for {
              body <- ZIO.fromEither(decode[CommentWebhook](jsonBody)).tapError(err =>
                ZIO.logError(s"Failed to decode ${err}")
              ).orDie
              _ <- Notifications.commentOnMergeRequest(
                body.user.name,
                body.merge_request.get.iid,
                body.object_attributes.url,
                body.object_attributes.note,
                body.project.name,
              )
            } yield StatusCode.Ok
          case "merge request hook" =>
            for {
              body <- ZIO.fromEither(decode[MergeRequestHook](jsonBody)).tapError(err =>
                ZIO.logError(s"Failed to decode ${err}")
              ).orDie
              _ <- Notifications.mergeRequest(
                body.object_attributes.action,
                body.user.name,
                body.object_attributes.iid,
                body.object_attributes.url,
                body.object_attributes.source_branch,
                body.project.name,
              )
            } yield StatusCode.Ok

          case _ =>
            ZIO.logInfo(s"Unimplemented event received: ${jsonBody}")
              *> ZIO.succeed(StatusCode.NoContent)
      }

  private val service = ArmeriaZioServerInterpreter().toService(gitlabLogic)

  private val server = armeria.server.Server.builder().service(service).http(8081).build()

  val live: RLayer[Any, Unit] = ZLayer.scoped {
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
}