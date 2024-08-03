package software

import sttp.client3.{Request, Response, SttpBackend}
import sttp.model.StatusCode
import sttp.tapir.DecodeResult
import zio.{Task, ZIO}

package object bool {
  extension [E, T](r: Task[Response[DecodeResult[Either[E, T]]]]) def toZIO: Task[T] = {
    r.flatMap(resp => resp.body match
      case failure: DecodeResult.Failure => ZIO.fail(new Exception("Request failed to decode"))
      case DecodeResult.Value(v) => ZIO.fromEither(v).mapError(s => new Exception(s"Request to `${resp.request.uri}` failed")))
  }
}
