package io.github.lvicentesanchez.babel.sharding.regions.translation

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.UserID
import io.github.lvicentesanchez.data.Content

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait TranslationAPI {
  def sendMessage(userID: UserID, content: Content): Future[Unit]
}

final class TranslationAPIImpl private[translation] (ref: ActorRef, timeout: FiniteDuration) extends TranslationAPI {
  import Translation._

  override def sendMessage(userID: UserID, content: Content): Future[Unit] =
    ask(ref, Protocol.SendMessage(userID, content))(timeout).mapTo[Unit]
}
