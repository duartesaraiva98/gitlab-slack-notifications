package software.bool.domain

import software.bool.server.{CommentHook, MergeRequestHook}
import software.bool.slack.SlackApi
import zio.{RLayer, Task, ZIO, ZLayer}

case class Notification(
                         destinees: Destinees,
                         text: Text
                       )

object Notification {

  class Resolver(
                  destinees: Destinees.Resolver,
                ) {
    def of(hook: CommentHook): Task[Option[Notification]] =
      for {
        destinees <- destinees.from(hook)
        text <- Text.from(hook)
      } yield Some(Notification(destinees, text))

    def of(hook: MergeRequestHook): Task[Option[Notification]] =
      for {
        destinees <- destinees.from(hook)
        text <- Text.from(hook)
      } yield Some(Notification(destinees, text))
  }
  
  val live: RLayer[Destinees.Resolver, Resolver] = ZLayer.fromFunction(new Resolver(_))

  extension (task: Task[Option[Notification]]) def run(slackApi: SlackApi): Task[Unit] =
    task.flatMap(_.map(slackApi.send).getOrElse(ZIO.unit))
}
