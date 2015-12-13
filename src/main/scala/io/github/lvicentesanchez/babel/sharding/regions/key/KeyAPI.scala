package io.github.lvicentesanchez.babel.sharding.regions.key

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.UserID
import io.github.lvicentesanchez.data.Content

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait KeyAPI {
  def sendMessage(userID: UserID, content: Content): Future[Unit]
}

final class KeyAPIImpl private[key] (ref: ActorRef, timeOut: FiniteDuration) extends KeyAPI {
  import Key._

  override def sendMessage(userID: UserID, content: Content): Future[Unit] =
    ask(ref, Protocol.SendMessage(userID, content))(timeOut).mapTo[Unit]
}
