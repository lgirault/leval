//package leval.network
//
//import java.util.concurrent.TimeUnit
//
//import akka.actor.ActorSystem
//import akka.util.Timeout
//import com.typesafe.config.{Config, ConfigFactory}
//import org.scalatest.{ConfigMap, Suite, BeforeAndAfterAll}
//
//trait ActorSystemSetup extends BeforeAndAfterAll {
//  self : Suite =>
//
//  val port : Int
//
//  def config(port: Int): Config = {
//    val configStr =
//      "akka.remote.netty.tcp.hostname = 127.0.0.1\n" +
//        "akka.remote.netty.tcp.port = " + port + "\n"
//
//    ConfigFactory.parseString(configStr)
//  }
//
//
//  lazy implicit val system = ActorSystem(s"TestSystem-$port",
//    config(port))
//
//  implicit val requestTimeOut = Timeout(100, TimeUnit.MILLISECONDS)
//
//
//  override def afterAll(configMap: ConfigMap): Unit = {
//    system.terminate()
//    super.afterAll()
//  }
//}
