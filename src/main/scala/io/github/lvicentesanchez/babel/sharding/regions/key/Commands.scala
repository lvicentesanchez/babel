package io.github.lvicentesanchez.babel.sharding.regions.key

import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.Command

private[key] trait Commands {
  sealed case class AddLocale(projectID: ProjectID, keyID: KeyID, locale: String) extends Command
  sealed case class CreateKey(projectID: ProjectID, keyID: KeyID) extends Command
  sealed case class KeyExists(projectID: ProjectID, keyID: KeyID) extends Command
  sealed case class LocaleExists(projectID: ProjectID, keyID: KeyID, locale: String) extends Command
}
