package io.github.lvicentesanchez.babel.sharding.cluster

import akka.actor.{ ActorRef, Props }
import akka.cluster.sharding.ShardRegion

trait Blueprint[A] {
  def extractID: ShardRegion.ExtractEntityId
  def name: String
  def props: Props
  def region(ref: ActorRef): Shard[A]
}
