package software.bool.slack

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.users.UsersLookupByEmailRequest
import com.slack.api.model.Attachment
import software.bool.domain.Notification
import scala.jdk.CollectionConverters._
import zio.{RIO, RLayer, Task, UIO, ZIO, ZLayer}

class SlackApi(config: SlackConfig, slack: Slack) {

  val methods = slack.methods(config.token)

  def send(notification: Notification): Task[Unit] = ZIO.foreachPar(notification.destinees.userIds)(userId =>
    for {
      resp <- ZIO.attemptBlocking(
        methods.chatPostMessage(
          ChatPostMessageRequest.builder()
            .text(notification.text.text)
            .attachments(notification.text.attachment.map(a => Attachment.builder().text(a).build()).toSeq.asJava)
            .channel(userId)
            .build()
        )
      )
      _ <- ZIO.fail(new Exception(s"Slack request failed: ${resp.getError}")).unless(resp.isOk)
    } yield ()
  ).unit

  def userByEmail(email: String): Task[String] =
    for {
      resp <- ZIO.attemptBlocking(
        methods.usersLookupByEmail(UsersLookupByEmailRequest.builder().email(email).build())
      )
      _ <- ZIO.fail(new Exception(s"Slack request failed: ${resp.getError}")).unless(resp.isOk)
    } yield resp.getUser.getId
}

object SlackApi {

  val live: RLayer[SlackConfig & Slack, SlackApi] = ZLayer.fromFunction(new SlackApi(_, _))

}
