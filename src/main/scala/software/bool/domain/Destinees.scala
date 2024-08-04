package software.bool.domain

import software.bool.domain.Destinees.DestineesConfig.DestineesResolverType
import software.bool.gitlab.GitlabApi
import software.bool.gitlab.GitlabConfig.config
import software.bool.server.{CommentHook, MergeRequestHook}
import software.bool.slack.SlackApi
import zio.config.*
import zio.Config.string
import zio.{Config, ConfigProvider, RLayer, Task, ZIO, ZLayer}

object Destinees {

  val live: RLayer[GitlabApi & SlackApi & DestineesConfig & GitlabIdSlackIdMap, Destinees.Resolver] = ZLayer.fromZIO {
    ZIO.service[DestineesConfig].flatMap { conf =>
      DestineesResolverType.valueOf(conf.resolver) match
        case DestineesResolverType.EqualEmail =>
          for {
            gitlabApi <- ZIO.service[GitlabApi]
            slackApi <- ZIO.service[SlackApi]
          } yield new EqualEmailResolver(gitlabApi, slackApi)
        case DestineesResolverType.StaticId =>
          for {
            gitlabApi <- ZIO.service[GitlabApi]
            gitlabIdSlackIdMap <- ZIO.service[GitlabIdSlackIdMap]
          } yield new StaticIdMapResolver(gitlabApi, gitlabIdSlackIdMap)
    }
  }
    ZLayer.fromFunction(new EqualEmailResolver(_, _))

  case class DestineesConfig(resolver: String)

  object DestineesConfig {
    private val config: Config[DestineesConfig] = (string("DESTINEES_RESOLVER").withDefault("EqualEmail")).to[DestineesConfig]

    val live: RLayer[ConfigProvider, DestineesConfig] = ZLayer.fromZIO {
      for {
        provider <- ZIO.service[ConfigProvider]
        conf <- provider.load(config).orDie
      } yield conf
    }

    enum DestineesResolverType:
      case EqualEmail extends DestineesResolverType
      case StaticId extends DestineesResolverType
  }

  class EqualEmailResolver(gitlabApi: GitlabApi, slackApi: SlackApi) extends Resolver {
    override def from(hook: CommentHook): Task[Destinees] =
      hook.merge_request.map { mr =>
        for {
          participants <- gitlabApi.mergeRequestParticipants(hook.project.id, mr.iid)
          gitlabEmails <- ZIO.foreachPar(participants.map(_.id))(gitlabApi.userById(_).map(_.public_email))
          slackUserIds <- ZIO.foreachPar(gitlabEmails)(slackApi.userByEmail)
        } yield Destinees(slackUserIds)
      }.getOrElse(ZIO.succeed(Destinees(Seq.empty[String])))

    override def from(hook: MergeRequestHook): Task[Destinees] = {
      for {
        participants <- gitlabApi.mergeRequestParticipants(hook.project.id, hook.object_attributes.iid)
        gitlabEmails <- ZIO.foreachPar(participants.map(_.id))(gitlabApi.userById(_).map(_.public_email))
        slackUserIds <- ZIO.foreachPar(gitlabEmails)(slackApi.userByEmail)
      } yield Destinees(slackUserIds)
    }
  }

  class StaticIdMapResolver(gitlabApi: GitlabApi, idMap: GitlabIdSlackIdMap) extends Resolver {
    override def from(hook: CommentHook): Task[Destinees] =
      hook.merge_request.map { mr =>
        for {
          participants <- gitlabApi.mergeRequestParticipants(hook.project.id, mr.iid)
          slackUserIds = participants.flatMap(p => idMap.get(p.id))
        } yield Destinees(slackUserIds)
      }.getOrElse(ZIO.succeed(Destinees(Seq.empty[String])))

    override def from(hook: MergeRequestHook): Task[Destinees] = {
      for {
        participants <- gitlabApi.mergeRequestParticipants(hook.project.id, hook.object_attributes.iid)
        slackUserIds = participants.flatMap(p => idMap.get(p.id))
      } yield Destinees(slackUserIds)
    }
  }

  trait Resolver {
    def from(hook: CommentHook): Task[Destinees]

    def from(hook: MergeRequestHook): Task[Destinees]
  }

}

case class Destinees(userIds: Seq[String])