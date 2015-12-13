package io.github.lvicentesanchez.babel.sharding.regions.project

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.UserID
import io.github.lvicentesanchez.data.Content

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait ProjectAPI {
  def sendMessage(userID: UserID, content: Content): Future[Unit]
}

final class ProjectAPIImpl private[project] (ref: ActorRef, timeout: FiniteDuration) extends ProjectAPI {
  import Project._

  override def sendMessage(userID: UserID, content: Content): Future[Unit] =
    ask(ref, Protocol.SendMessage(userID, content))(timeout).mapTo[Unit]
}
