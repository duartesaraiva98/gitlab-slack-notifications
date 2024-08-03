package software.bool.domain

import software.bool.gitlab.GitlabApi
import software.bool.server.{CommentHook, MergeRequestHook}
import software.bool.slack.SlackApi
import zio.{RLayer, Task, ZIO, ZLayer}

object Destinees {
  
  val live: RLayer[GitlabApi & SlackApi, Destinees.Resolver] = ZLayer.fromFunction(new Resolver(_,_))

  class Resolver(gitlabApi: GitlabApi, slackApi: SlackApi) {
    def from(hook: CommentHook): Task[Destinees] =
      hook.merge_request.map { mr =>
        for {
          participants <- gitlabApi.mergeRequestParticipants(hook.project.id, mr.iid)
          gitlabEmails <- ZIO.foreachPar(participants.map(_.id))(gitlabApi.userById(_).map(_.public_email))
          slackUserIds <- ZIO.foreachPar(gitlabEmails)(slackApi.userByEmail)
        } yield Destinees(slackUserIds)
      }.getOrElse(ZIO.succeed(Destinees(Seq.empty[String])))

    def from(hook: MergeRequestHook): Task[Destinees] = {
      for {
        participants <- gitlabApi.mergeRequestParticipants(hook.project.id, hook.object_attributes.iid)
        gitlabEmails <- ZIO.foreachPar(participants.map(_.id))(gitlabApi.userById(_).map(_.public_email))
        slackUserIds <- ZIO.foreachPar(gitlabEmails)(slackApi.userByEmail)
      } yield Destinees(slackUserIds)
    }
  }

}

case class Destinees(userIds: Seq[String])