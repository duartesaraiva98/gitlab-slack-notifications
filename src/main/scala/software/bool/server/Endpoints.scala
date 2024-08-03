package software.bool.server

import sttp.tapir.ztapir.*


object Endpoints {
    val gitlab =
        endpoint
        .post
        .in("gitlab" / "webhook")
        .in(header[String]("X-Gitlab-Event").and(stringBody))
        .out(statusCode)
}