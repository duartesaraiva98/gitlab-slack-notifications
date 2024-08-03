package software.boolean

import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*

object Endpoints {
    val gitlab =
        endpoint
        .post
        .in("gitlab" / "webhook")
        .in(header[String]("X-Gitlab-Event").and(stringBody))
        .out(statusCode)
}