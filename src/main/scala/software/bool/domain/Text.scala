package software.bool.domain

import software.bool.server.{CommentHook, MergeRequestHook}
import zio.{Task, ZIO}

case class Text(text: String, attachment: Option[String])

object Text {

  def from(hook: CommentHook): Task[Text] =
    ZIO.fromOption(hook.merge_request.map { mr =>
      Text(s"${hook.user.name} has commented on merge request <${hook.object_attributes.url}|${mr.iid}> in <${hook.project.web_url}|${hook.project.name}>\n", Some(hook.object_attributes.note))
    }).orElseFail(new Exception(""))

  def from(hook: MergeRequestHook): Task[Text] =
    ZIO.succeed(Text(s"${hook.user.name} ${MergeRequestAction.valueOf(hook.object_attributes.action).label} merge request <${hook.object_attributes.url}|${hook.object_attributes.iid}> for branch *${hook.object_attributes.source_branch}* in <${hook.project.web_url}|${hook.project.name}>", None))

}
