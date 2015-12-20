package io.github.lvicentesanchez.babel

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.github.lvicentesanchez.babel.api.BabelAPIImpl
import io.github.lvicentesanchez.babel.data.{ KeyID, ProjectID }
import io.github.lvicentesanchez.babel.sharding.cluster.Cluster
import io.github.lvicentesanchez.babel.sharding.regions.key.Key
import io.github.lvicentesanchez.babel.sharding.regions.project.Project
import io.github.lvicentesanchez.babel.sharding.regions.translation.Translation

import scala.concurrent.ExecutionContext.Implicits.global

object Babel extends App {
  val port = "2551"
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load())

  val system = ActorSystem("RosettaSystem", config)
  val sharding = Cluster(system, 100)
  val key = sharding.of(Key.Blueprint)
  val project = sharding.of(Project.Blueprint)
  val translation = sharding.of(Translation.Blueprint)
  val babelAPI = new BabelAPIImpl(key.api, project.api, translation.api)

  val f0 =
    for {
      a <- babelAPI.createProject(ProjectID("warriors"))
      _ <- babelAPI.createKey(ProjectID("warriors"), KeyID("winner"))
      _ <- babelAPI.addLocale(ProjectID("warriors"), KeyID("winner"), "en")
      _ <- babelAPI.addLocale(ProjectID("warriors"), KeyID("winner"), "es")
      _ <- babelAPI.addLocale(ProjectID("warriors"), KeyID("winner"), "fr")
      _ <- babelAPI.createTranslation(ProjectID("warriors"), KeyID("winner"), "en", "winner")
      _ <- babelAPI.createTranslation(ProjectID("warriors"), KeyID("winner"), "es", "campeon")
      _ <- babelAPI.createTranslation(ProjectID("warriors"), KeyID("winner"), "fr", "champion")
    } yield ()

  val f1 =
    for {
      _ <- f0
      es <- babelAPI.getTranslation(ProjectID("warriors"), KeyID("winner"), "en")
      en <- babelAPI.getTranslation(ProjectID("warriors"), KeyID("winner"), "es")
      fr <- babelAPI.getTranslation(ProjectID("warriors"), KeyID("winner"), "fr")
    } yield (es, en, fr)

  f1.foreach(println)
}
