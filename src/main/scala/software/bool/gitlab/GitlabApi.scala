package software.bool.gitlab

import zio.{Task, UIO, ZIO, ZLayer}
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.client3.*
import sttp.client3.armeria.zio.*
import sttp.model.StatusCode
import sttp.tapir.DecodeResult
import software.bool.toZIO

class GitlabApi(
                 config: GitlabConfig,
                 sttpBackend: SttpBackend[Task, Any]
              ) {

  def userById(id: Int): ZIO[Any, Throwable, GitlabUser] = {
    val request: Request[DecodeResult[Either[StatusCode, GitlabUser]], Any] = SttpClientInterpreter()
      .toRequest(GitlabApi.userByIdEndpoint, Some(uri"${config.url}"))
      .apply((id, config.token))

    sttpBackend.send(request).toZIO
  }

  def mergeRequestParticipants(projectId: Int, mergeRequestIid: Int): Task[Seq[GitlabMergeRequestParticipant]] = {
    val request: Request[DecodeResult[Either[StatusCode, Seq[GitlabMergeRequestParticipant]]], Any] = SttpClientInterpreter()
      .toRequest(GitlabApi.mergeRequestParticipantsEndpoint, Some(uri"${config.url}"))
      .apply((projectId, mergeRequestIid, config.token))

    sttpBackend.send(request).toZIO
  }

}

object GitlabApi {
  
  val live = ZLayer.fromFunction(new GitlabApi(_, _))

  val userByIdEndpoint =
    endpoint
      .get
      .in("api" / "v4" / "users" / path[Int]("id"))
      .in(header[String]("PRIVATE-TOKEN"))
      .out(jsonBody[GitlabUser])
      .errorOut(statusCode)

  val mergeRequestParticipantsEndpoint =
    endpoint
      .get
      .in("api" / "v4" / "projects" / path[Int]("id") / "merge_requests" / path[Int]("merge_request_iid") / "participants")
      .in(header[String]("PRIVATE-TOKEN"))
      .out(jsonBody[Seq[GitlabMergeRequestParticipant]])
      .errorOut(statusCode)
}