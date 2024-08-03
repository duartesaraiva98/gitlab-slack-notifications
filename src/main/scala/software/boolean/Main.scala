package software.boolean

import zio.Console.ConsoleLive
import zio.{Scope, Tag, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {

  implicit val appRuntime: zio.Runtime[Any] = runtime

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Unit] =
    ZLayer.make[Unit](
      Server().live
    )

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] = ZIO.logInfo("Bootstrap completed") *> ZIO.unit.forever
}
