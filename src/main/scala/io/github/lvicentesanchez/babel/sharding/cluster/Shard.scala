package io.github.lvicentesanchez.babel.sharding.cluster

trait Shard[A] {
  def api: A
}
