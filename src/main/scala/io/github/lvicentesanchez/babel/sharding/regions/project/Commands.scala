package io.github.lvicentesanchez.babel.sharding.regions.project

import io.github.lvicentesanchez.babel.data.ProjectID
import io.github.lvicentesanchez.babel.sharding.Command

private[project] trait Commands {
  sealed case class CreateProject(projectID: ProjectID) extends Command
  sealed case class ProjectExists(projectID: ProjectID) extends Command
}
