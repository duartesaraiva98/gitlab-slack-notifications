package software.bool.gitlab

import io.circe.Codec
import sttp.tapir.Schema

case class GitlabUser(id: Int, public_email: String) derives Codec, Schema
