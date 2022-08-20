package gp.leval

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    LevalServer.stream[IO].compile.drain.as(ExitCode.Success)
}
