package io.github.lvicentesanchez.babel.sharding.regions.translation

import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.Command

private[translation] trait Commands {
  sealed case class CreateTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String) extends Command
  sealed case class GetTranslation(projectID: ProjectID, keyID: KeyID, locale: String) extends Command
}
