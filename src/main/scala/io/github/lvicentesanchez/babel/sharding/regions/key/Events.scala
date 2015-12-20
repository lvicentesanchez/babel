package io.github.lvicentesanchez.babel.sharding.regions.key

import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.Event

private[key] trait Events {
  sealed case class KeyCreated(projectID: ProjectID, keyID: KeyID) extends Event
  sealed case class LocaleAdded(projectID: ProjectID, keyID: KeyID, locale: String) extends Event
}
