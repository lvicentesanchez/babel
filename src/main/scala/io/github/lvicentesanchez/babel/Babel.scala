package io.github.lvicentesanchez.babel

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.github.lvicentesanchez.babel.data.ProjectID
import io.github.lvicentesanchez.babel.sharding.cluster.Cluster
import io.github.lvicentesanchez.babel.sharding.regions.key.Key
import io.github.lvicentesanchez.babel.sharding.regions.project.Project
import io.github.lvicentesanchez.babel.sharding.regions.translation.Translation

import scala.concurrent.{ Future, blocking }
import scala.concurrent.ExecutionContext.Implicits.global

object Babel extends App {
  val port = "2551"
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load())

  val system = ActorSystem("RosettaSystem", config)
  val sharding = Cluster(system, 100)
  val project = sharding.of(Project.blueprint)
  val translation = sharding.of(Translation.blueprint)
  val key = sharding.of(Key.blueprint(project.api, translation.api))

  for {
    r0 <- project.api.projectExists(ProjectID("a"))
    _ = println(s"Project a exists? $r0")
    r1 <- project.api.createProject(ProjectID("a"))
    r2 <- Future(blocking(Thread.sleep(15000)))
    r3 <- project.api.projectExists(ProjectID("a"))
    _ = println(s"Project a exists? $r3")
  } yield ()
}
