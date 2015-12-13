package io.github.lvicentesanchez.babel.sharding.regions.project

import akka.actor.ActorRef
import akka.pattern.ask
import io.github.lvicentesanchez.babel.data.ProjectID

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait ProjectAPI {
  def createProject(projectID: ProjectID): Future[Boolean]
  def projectExists(projectID: ProjectID): Future[Boolean]
}

final class ProjectAPIImpl private[project] (ref: ActorRef, timeout: FiniteDuration) extends ProjectAPI {
  import Project._

  override def createProject(projectID: ProjectID): Future[Boolean] =
    ask(ref, Commands.CreateProject(projectID))(timeout).mapTo[Boolean]

  override def projectExists(projectID: ProjectID): Future[Boolean] =
    ask(ref, Commands.ProjectExists(projectID))(timeout).mapTo[Boolean]
}
