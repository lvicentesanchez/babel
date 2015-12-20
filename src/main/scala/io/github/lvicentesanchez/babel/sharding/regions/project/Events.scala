package io.github.lvicentesanchez.babel.sharding.regions.project

import io.github.lvicentesanchez.babel.data.ProjectID
import io.github.lvicentesanchez.babel.sharding.Event

private[project] trait Events {
  sealed case class ProjectCreated(projectID: ProjectID) extends Event
}
