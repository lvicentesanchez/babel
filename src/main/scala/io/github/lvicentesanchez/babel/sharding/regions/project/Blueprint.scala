package io.github.lvicentesanchez.babel.sharding.regions.project

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion._
import io.github.lvicentesanchez.babel.sharding.{ cluster => C }

import scala.concurrent.duration._

private[project] trait Blueprint extends C.Blueprint[ProjectAPI] {
  import Project.Commands._

  override val extractID: ExtractEntityId = {
    case msg: CreateProject => (msg.projectID.value, msg)
    case msg: ProjectExists => (msg.projectID.value, msg)
  }

  override val name: String = "Project"

  override val props: Props = Props(Project())

  override def region(ref: ActorRef): C.Shard[ProjectAPI] =
    new C.Shard[ProjectAPI] {
      override val api: ProjectAPI = new ProjectAPIImpl(ref, 10.seconds)
    }
}
