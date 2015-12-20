package io.github.lvicentesanchez.babel.sharding.regions.translation

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion._
import io.github.lvicentesanchez.babel.sharding.{ cluster => C }

import scala.concurrent.duration._

private[translation] trait Blueprint extends C.Blueprint[TranslationAPI] {
  import Translation.Commands._

  override val extractID: ExtractEntityId = {
    case msg: CreateTranslation => (s"${msg.projectID.value}/${msg.keyID.value}/${msg.locale}", msg)
    case msg: GetTranslation => (s"${msg.projectID.value}/${msg.keyID.value}/${msg.locale}", msg)
  }

  override val name: String = "Translation"

  override val props: Props = Props(Translation())

  override def region(ref: ActorRef): C.Shard[TranslationAPI] =
    new C.Shard[TranslationAPI] {
      override val api: TranslationAPI = new TranslationAPIImpl(ref, 10.seconds)
    }
}