package io.github.lvicentesanchez.babel.sharding.regions.project

import java.net.URLDecoder

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import io.github.lvicentesanchez.babel.data.ProjectID

import scala.concurrent.duration._
import scala.io.Codec

final class Project private[project] () extends Actor with PersistentActor {
  import Project._

  var projectID: Option[ProjectID] = None

  context.setReceiveTimeout(10.seconds)

  override def receiveRecover: Receive = {
    case evt: Events.ProjectCreated =>
      projectID = Option(evt.projectID)
  }

  override def receiveCommand: Receive = {
    case msg: Commands.CreateProject if projectID.isEmpty =>
      persist(Events.ProjectCreated(msg.projectID)) {
        case Events.ProjectCreated(id) =>
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

  override val persistenceId: String = URLDecoder.decode(self.path.name, Codec.UTF8.name)
}

object Project {
  private[project] def apply(): Project = new Project()

  object Blueprint extends Blueprint

  object Commands extends Commands

  object Events extends Events
}
