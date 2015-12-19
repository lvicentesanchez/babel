package io.github.lvicentesanchez.babel.sharding.regions.key

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait KeyAPI {
  def addLocale(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean]
  def createKey(projectID: ProjectID, keyID: KeyID): Future[Boolean]
  def keyExists(projectID: ProjectID, keyID: KeyID): Future[Boolean]
  def localeExists(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean]
}

final class KeyAPIImpl private[key] (ref: ActorRef, timeout: FiniteDuration) extends KeyAPI {
  import Key._

  override def addLocale(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean] =
    ask(ref, Commands.AddLocale(projectID, keyID, locale.toUpperCase))(timeout).mapTo[Boolean]

  override def createKey(projectID: ProjectID, keyID: KeyID): Future[Boolean] =
    ask(ref, Commands.CreateKey(projectID, keyID))(timeout).mapTo[Boolean]

  override def keyExists(projectID: ProjectID, keyID: KeyID): Future[Boolean] =
    ask(ref, Commands.KeyExists(projectID, keyID))(timeout).mapTo[Boolean]

  override def localeExists(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean] =
    ask(ref, Commands.LocaleExists(projectID, keyID, locale.toUpperCase))(timeout).mapTo[Boolean]
}
