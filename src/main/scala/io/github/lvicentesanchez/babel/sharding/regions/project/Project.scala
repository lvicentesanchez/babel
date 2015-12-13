package io.github.lvicentesanchez.babel.sharding.regions.project

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.ExtractEntityId
import akka.persistence.PersistentActor
import io.github.lvicentesanchez.babel.data.UserID
import io.github.lvicentesanchez.babel.sharding._
import io.github.lvicentesanchez.babel.sharding.cluster.{ Blueprint, Shard }
import io.github.lvicentesanchez.data.Content

import scala.concurrent.duration._

final class Project private[project] () extends Actor with PersistentActor {
  import Project._

  context.setReceiveTimeout(10.seconds)

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case msg: Protocol.SendMessage =>
      println(s"Actor ${context.self.path.name} received $msg")

    case ReceiveTimeout =>
      context.parent ! ShardRegion.Passivate(stopMessage = SupervisorStrategy.Stop)

    case SupervisorStrategy.Stop =>
      println(s"Stop actor $persistenceId")
      context.stop(self)
  }

  override val persistenceId: String = self.path.name
}

object Project {
  val blueprint: Blueprint[ProjectAPI] =
    new Blueprint[ProjectAPI] {
      import Project.Protocol._

      override val extractID: ExtractEntityId = {
        case msg: SendMessage => (msg.userID.value, msg)
      }

      override val name: String = "Project"

      override val props: Props = Props(new Project())

      override def region(ref: ActorRef): Shard[ProjectAPI] =
        new Shard[ProjectAPI] {
          override val api: ProjectAPI = new ProjectAPIImpl(ref, 10.seconds)
        }
    }

  object Protocol {
    final case class SendMessage(userID: UserID, content: Content) extends Command
  }
}
