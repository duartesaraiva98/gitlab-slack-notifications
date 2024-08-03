package software.bool.gitlab

import io.circe.Codec
import sttp.tapir.Schema

case class GitlabMergeRequestParticipant(id: Int, username: String, state: String) derives Codec, Schema