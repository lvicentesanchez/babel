package io.github.lvicentesanchez.babel.sharding.regions.key

import java.net.URLDecoder

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }

import scala.concurrent.duration._
import scala.io.Codec

final class Key private[key] () extends Actor with PersistentActor {
  import Key._

  var keyID: Option[KeyID] = None
  var projectID: Option[ProjectID] = None
  var locales: List[String] = List()

  context.setReceiveTimeout(10.seconds)

  override def receiveRecover: Receive = {
    case evt: Events.KeyCreated =>
      projectID = Option(evt.projectID)
  }

  override def receiveCommand: Receive = {
    case msg: Commands.AddLocale if (projectID.isEmpty && keyID.isEmpty) || locales.contains(msg.locale) =>
      sender() ! false

    case msg: Commands.AddLocale =>
      persist(Events.LocaleAdded(msg.projectID, msg.keyID, msg.locale)) {
        case Events.LocaleAdded(_, _, locale) =>
          locales ::= locale
          sender() ! true
      }

    case msg: Commands.CreateKey if projectID.isEmpty && keyID.isEmpty =>
      persist(Events.KeyCreated(msg.projectID, msg.keyID)) {
        case Events.KeyCreated(pid, kid) =>
          keyID = Option(kid)
          projectID = Option(pid)
          sender() ! true
      }

    case msg: Commands.CreateKey =>
      sender() ! false

    case msg: Commands.KeyExists =>
      sender() ! projectID.flatMap(pid => keyID.map((pid, _))).fold(false)((msg.projectID, msg.keyID) == _)

    case msg: Commands.LocaleExists =>
      sender() ! projectID.flatMap(pid => keyID.map((pid, _))).fold(false)(data => (msg.projectID, msg.keyID) == data && locales.contains(msg.locale))

    case ReceiveTimeout =>
      context.parent ! ShardRegion.Passivate(stopMessage = SupervisorStrategy.Stop)

    case SupervisorStrategy.Stop =>
      println(s"""Stopping actor "$persistenceId"""")
      context.stop(self)
  }

  override val persistenceId: String = URLDecoder.decode(self.path.name, Codec.UTF8.name)
}

object Key {
  private[key] def apply(): Key = new Key()

  object Blueprint extends Blueprint

  object Commands extends Commands

  object Events extends Events
}
