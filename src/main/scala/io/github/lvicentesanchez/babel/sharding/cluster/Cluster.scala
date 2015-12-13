package io.github.lvicentesanchez.babel.sharding.cluster

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

final class Cluster private[cluster] (system: ActorSystem, nrOfShards: Int) {
  def of[A](blueprint: Blueprint[A]): Shard[A] = {
    val extractShardID: ShardRegion.ExtractShardId =
      blueprint.extractID andThen {
        case (id, _) => String.valueOf(id.## % nrOfShards)
      }
    val ref: ActorRef =
      ClusterSharding(system).start(
        blueprint.name,
        blueprint.props,
        ClusterShardingSettings(system),
        blueprint.extractID,
        extractShardID
      )
    blueprint.region(ref)
  }
}

object Cluster {
  def apply(system: ActorSystem, nrOfShards: Int): Cluster = new Cluster(system, nrOfShards)
}
