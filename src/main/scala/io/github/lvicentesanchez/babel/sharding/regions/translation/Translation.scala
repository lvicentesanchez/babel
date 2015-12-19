package io.github.lvicentesanchez.babel.sharding.regions.translation

import java.net.URLDecoder

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.ExtractEntityId
import akka.persistence.PersistentActor
import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding._
import io.github.lvicentesanchez.babel.sharding.cluster.{ Blueprint, Shard }

import scala.concurrent.duration._
import scala.io.Codec

final class Translation private[translation] () extends Actor with PersistentActor {
  import Translation._

  var keyID: Option[KeyID] = None
  var projectID: Option[ProjectID] = None
  var locale: Option[String] = None
  var translation: Option[String] = None

  context.setReceiveTimeout(10.seconds)

  override def receiveRecover: Receive = {
    case evt: Events.TranslationCreated =>
      keyID = Option(evt.keyID)
      projectID = Option(evt.projectID)
      locale = Option(evt.locale)
      translation = Option(evt.value)
  }

  override def receiveCommand: Receive = {
    case msg: Commands.CreateTranslation if projectID.isEmpty && keyID.isEmpty && locale.isEmpty && translation.isEmpty =>
      persist(Events.TranslationCreated(msg.projectID, msg.keyID, msg.locale, msg.value)) {
        case Events.TranslationCreated(pid, kid, loc, trn) =>
          keyID = Option(kid)
          projectID = Option(pid)
          locale = Option(loc)
          translation = Option(trn)
          sender() ! true
      }

    case msg: Commands.CreateTranslation =>
      persist(Events.TranslationCreated(msg.projectID, msg.keyID, msg.locale, msg.value)) {
        case Events.TranslationCreated(_, _, _, trn) =>
          translation = Option(trn)
          sender() ! true
      }

    case msg: Commands.GetTranslation =>
      sender() ! translation

    case ReceiveTimeout =>
      context.parent ! ShardRegion.Passivate(stopMessage = SupervisorStrategy.Stop)

    case SupervisorStrategy.Stop =>
      println(s"""Stopping actor "$persistenceId"""")
      context.stop(self)
  }

  override val persistenceId: String = URLDecoder.decode(self.path.name, Codec.UTF8.name)
}

object Translation {
  val blueprint: Blueprint[TranslationAPI] =
    new Blueprint[TranslationAPI] {
      import Translation.Commands._

      override val extractID: ExtractEntityId = {
        case msg: CreateTranslation => (s"${msg.projectID.value}/${msg.keyID.value}/${msg.locale}", msg)
        case msg: GetTranslation => (s"${msg.projectID.value}/${msg.keyID.value}/${msg.locale}", msg)
      }

      override val name: String = "Translation"

      override val props: Props = Props(new Translation())

      override def region(ref: ActorRef): Shard[TranslationAPI] =
        new Shard[TranslationAPI] {
          override val api: TranslationAPI = new TranslationAPIImpl(ref, 10.seconds)
        }
    }

  object Commands {
    final case class CreateTranslation(projectID: ProjectID, keyID: KeyID, locale: String, value: String) extends Command
    final case class GetTranslation(projectID: ProjectID, keyID: KeyID, locale: String) extends Command
  }

  object Events {
    final case class TranslationCreated(projectID: ProjectID, keyID: KeyID, locale: String, value: String) extends Event
  }
}
