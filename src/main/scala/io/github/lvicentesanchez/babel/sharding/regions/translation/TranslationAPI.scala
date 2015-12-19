package io.github.lvicentesanchez.babel.sharding.regions.translation

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait TranslationAPI {
  def createTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String): Future[Boolean]
  def getTranslation(projectID: ProjectID, keyID: KeyID, locale: String): Future[Option[String]]
}

final class TranslationAPIImpl private[translation] (ref: ActorRef, timeout: FiniteDuration) extends TranslationAPI {
  import Translation._

  override def createTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String): Future[Boolean] =
    ask(ref, Commands.CreateTranslation(projectID, keyID, locale.toUpperCase, value))(timeout).mapTo[Boolean]

  override def getTranslation(projectID: ProjectID, keyID: KeyID, locale: String): Future[Option[String]] =
    ask(ref, Commands.GetTranslation(projectID, keyID, locale.toUpperCase))(timeout).mapTo[Option[String]]
}
