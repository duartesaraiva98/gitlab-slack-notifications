package software.bool.server

import io.circe.parser.decode
import software.bool.domain.Notification
import software.bool.slack.SlackApi
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.server.armeria.zio.ArmeriaZioServerInterpreter
import sttp.tapir.ztapir.*
import zio.{RLayer, ZIO, ZLayer}

import java.util.concurrent.CompletableFuture

class ServerLogic(resolver: Notification.Resolver, slackApi: SlackApi) {
  val gitlabLogic: ZServerEndpoint[Any, Any] =
    Endpoints
      .gitlab
      .zServerLogic { (header, jsonBody) =>
        header.toLowerCase match
          case "note hook" =>
            for {
              body <- ZIO.fromEither(decode[CommentHook](jsonBody)).tapError(err =>
                ZIO.logError(s"Failed to decode ${err}")
              ).orDie
              _ <- resolver.of(body).run(slackApi).logError.orDie
            } yield StatusCode.Ok
          case "merge request hook" =>
            for {
              body <- ZIO.fromEither(decode[MergeRequestHook](jsonBody)).tapError(err =>
                ZIO.logError(s"Failed to decode $err")
              ).orDie
              _ <- resolver.of(body).run(slackApi).logError.orDie
            } yield StatusCode.Ok
          case _ =>
            ZIO.logInfo(s"Unimplemented event received: ${jsonBody}")
              *> ZIO.succeed(StatusCode.NoContent)
      }
}