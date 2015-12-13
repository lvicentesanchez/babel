package io.github.lvicentesanchez.babel.sharding.regions.project

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.ExtractEntityId
import akka.persistence.{ PersistentActor, SnapshotOffer }
import io.github.lvicentesanchez.babel.data.ProjectID
import io.github.lvicentesanchez.babel.sharding._
import io.github.lvicentesanchez.babel.sharding.cluster.{ Blueprint, Shard }
import io.github.lvicentesanchez.babel.sharding.regions.project.Project.Events.ProjectCreated

import scala.concurrent.duration._

final class Project private[project] () extends Actor with PersistentActor {
  import Project._

  var projectID: Option[ProjectID] = None

  context.setReceiveTimeout(10.seconds)

  override def receiveRecover: Receive = {
    case evt: Events.ProjectCreated =>
      projectID = Option(evt.projectID)

    case SnapshotOffer(_, pid: ProjectID) =>
      projectID = Option(pid)
  }

  override def receiveCommand: Receive = {
    case msg: Commands.CreateProject if projectID.isEmpty =>
      persist(Events.ProjectCreated(msg.projectID)) {
        case ProjectCreated(id) =>
          projectID = Option(id)
          sender() ! true
      }

    case msg: Commands.CreateProject =>
      sender() ! false

    case msg: Commands.ProjectExists =>
      sender() ! projectID.fold(false)(msg.projectID == _)

    case ReceiveTimeout =>
      context.parent ! ShardRegion.Passivate(stopMessage = SupervisorStrategy.Stop)

    case SupervisorStrategy.Stop =>
      println(s"""Stopping actor "$persistenceId"""")
      context.stop(self)
  }

  override val persistenceId: String = s"project/${self.path.name}"
}

object Project {
  val blueprint: Blueprint[ProjectAPI] =
    new Blueprint[ProjectAPI] {
      import Project.Commands._

      override val extractID: ExtractEntityId = {
        case msg: CreateProject => (msg.projectID.value, msg)
        case msg: ProjectExists => (msg.projectID.value, msg)
      }

      override val name: String = "Project"

      override val props: Props = Props(new Project())

      override def region(ref: ActorRef): Shard[ProjectAPI] =
        new Shard[ProjectAPI] {
          override val api: ProjectAPI = new ProjectAPIImpl(ref, 10.seconds)
        }
    }

  object Commands {
    final case class CreateProject(projectID: ProjectID) extends Command
    final case class ProjectExists(projectID: ProjectID) extends Command
  }

  object Events {
    final case class ProjectCreated(projectID: ProjectID) extends Event
  }
}
