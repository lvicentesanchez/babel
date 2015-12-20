package io.github.lvicentesanchez.babel.sharding.regions.key

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion._
import io.github.lvicentesanchez.babel.sharding.{ cluster => C }

import scala.concurrent.duration._

private[key] trait Blueprint extends C.Blueprint[KeyAPI] {
  import Key.Commands._

  override val extractID: ExtractEntityId = {
    case msg: AddLocale => (s"${msg.projectID.value}/${msg.keyID.value}", msg)
    case msg: CreateKey => (s"${msg.projectID.value}/${msg.keyID.value}", msg)
    case msg: KeyExists => (s"${msg.projectID.value}/${msg.keyID.value}", msg)
    case msg: LocaleExists => (s"${msg.projectID.value}/${msg.keyID.value}", msg)
  }

  override val name: String = "Key"

  override val props: Props = Props(Key())

  override def region(ref: ActorRef): C.Shard[KeyAPI] =
    new C.Shard[KeyAPI] {
      override val api: KeyAPI = new KeyAPIImpl(ref, 10.seconds)
    }
}
