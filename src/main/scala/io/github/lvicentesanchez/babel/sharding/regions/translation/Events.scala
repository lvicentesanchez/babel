package io.github.lvicentesanchez.babel.sharding.regions.translation

import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.Event

private[translation] trait Events {
  sealed case class TranslationCreated(projectID: ProjectID, keyID: KeyID, locale: String, value: String) extends Event
}
