package olim7t

import akka.actor.{ActorLogging, Actor, Props}
import com.typesafe.config.ConfigFactory
import spray.routing.HttpServiceActor
import spray.can.server.SprayCanHttpServerApp

/** Bind everything together */
class RestInterface extends Actor
                    with ActorLogging
                    with HttpServiceActor
                    with ActorBackend
                    with RestApi {
  def receive = runRoute(routes)
}

object Main extends App with SprayCanHttpServerApp {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val api = system.actorOf(Props(new RestInterface()), "httpInterface")
  newHttpServer(api) ! Bind(interface = host, port = port)
}
