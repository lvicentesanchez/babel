package io.github.lvicentesanchez.babel

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.github.lvicentesanchez.babel.data.UserID
import io.github.lvicentesanchez.babel.sharding.cluster.Cluster
import io.github.lvicentesanchez.babel.sharding.regions.key.Key
import io.github.lvicentesanchez.babel.sharding.regions.project.Project
import io.github.lvicentesanchez.babel.sharding.regions.translation.Translation
import io.github.lvicentesanchez.data.Content

object Babel extends App {
  val port = "2551"
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load())

  val system = ActorSystem("RosettaSystem", config)
  val sharding = Cluster(system, 100)
  val project = sharding.of(Project.blueprint)
  val translation = sharding.of(Translation.blueprint)
  val key = sharding.of(Key.blueprint(project.api, translation.api))

  project.api.sendMessage(UserID("0"), Content("a"))
  project.api.sendMessage(UserID("0"), Content("b"))
  key.api.sendMessage(UserID("1"), Content("c"))
  translation.api.sendMessage(UserID("2"), Content("d"))
}
