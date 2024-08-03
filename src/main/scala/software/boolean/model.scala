package software.boolean

import io.circe.Codec
import sttp.tapir.Schema

case class CommentWebhook(
                           object_kind: String,
                           event_type: String,
                           user: User,
                           project: Project,
                           object_attributes: NoteObjectAttributes,
                           merge_request: Option[MergeRequest]
                         )derives Codec, Schema

case class MergeRequestHook(
                             object_kind: String,
                             event_type: String,
                             user: User,
                             project: Project,
                             object_attributes: MRObjectAttributes,
                           ) derives Codec

case class MergeRequest(
                         id: Int,
                         iid: Int,
                         title: String,
                         state: String,
                         draft: Boolean,
                       )derives Codec, Schema

case class NoteObjectAttributes(
                                 id: Int,
                                 note: String,
                                 noteable_type: String,
                                 author_id: Int,
                                 updated_at: String,
                                 url: String
                               )derives Codec, Schema

case class MRObjectAttributes(
                            iid: Int,
                            source_branch: String,
                            url: String,
                            action: String,
                             ) derives Codec

case class Project(
                    id: Int,
                    name: String,
                    web_url: String,
                  )derives Codec, Schema

case class User(
                 id: Int,
                 name: String,
                 username: String,
                 avatar_url: String,
                 email: String
               )derives Codec, Schema

