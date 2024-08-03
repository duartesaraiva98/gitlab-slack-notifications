package software.bool.domain

import software.bool.server.{CommentHook, MergeRequestHook}
import zio.{Task, ZIO}

case class Text(text: String)

object Text {

  def from(hook: CommentHook): Task[Text] =
    ZIO.fromOption(hook.merge_request.map { mr =>
      Text(s"${hook.user.name} has commented on merge request ${mr.id}${hook.object_attributes.url} in ${hook.project.name}: ${hook.object_attributes.note}")
    }).orElseFail(new Exception(""))
    
  def from(hook: MergeRequestHook): Task[Text] =
    ZIO.succeed(Text(s"${hook.user.name} ${hook.object_attributes.action} merge request ${hook.object_attributes.iid} for branch ${hook.object_attributes.source_branch} in ${hook.project.name}"))

}
