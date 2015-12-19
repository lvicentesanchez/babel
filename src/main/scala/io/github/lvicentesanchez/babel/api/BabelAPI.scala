package io.github.lvicentesanchez.babel.api

import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.regions.key.KeyAPI
import io.github.lvicentesanchez.babel.sharding.regions.project.ProjectAPI
import io.github.lvicentesanchez.babel.sharding.regions.translation.TranslationAPI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BabelAPI {
  def addLocale(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean]
  def createKey(projectID: ProjectID, keyID: KeyID): Future[Boolean]
  def createProject(projectID: ProjectID): Future[Boolean]
  def createTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String): Future[Boolean]
  def getTranslation(projectID: ProjectID, keyID: KeyID, locale: String): Future[Option[String]]
}

class BabelAPIImpl(keyAPI: KeyAPI, projectAPI: ProjectAPI, translationAPI: TranslationAPI) extends BabelAPI {
  override def addLocale(projectID: ProjectID, keyID: KeyID, locale: String): Future[Boolean] =
    for {
      keyExists <- keyAPI.keyExists(projectID, keyID)
      result <- if (keyExists) keyAPI.addLocale(projectID, keyID, locale) else Future.successful(false)
    } yield result

  override def createKey(projectID: ProjectID, keyID: KeyID): Future[Boolean] =
    for {
      keyExists <- keyAPI.keyExists(projectID, keyID)
      result <- if (!keyExists) keyAPI.createKey(projectID, keyID) else Future.successful(false)
    } yield result

  override def createProject(projectID: ProjectID): Future[Boolean] =
    for {
      projectExists <- projectAPI.projectExists(projectID)
      result <- if (!projectExists) projectAPI.createProject(projectID) else Future.successful(false)
    } yield result

  override def createTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String): Future[Boolean] =
    for {
      localeExists <- keyAPI.localeExists(projectID, keyID, locale)
      result <- if (localeExists) translationAPI.createTranslation(projectID, keyID, locale, value) else Future.successful(false)
    } yield result

  override def getTranslation(projectID: ProjectID, keyID: KeyID, locale: String): Future[Option[String]] =
    translationAPI.getTranslation(projectID, keyID, locale)
}
