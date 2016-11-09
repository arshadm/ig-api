package io.spinor.ig.api.streaming

import com.typesafe.config.ConfigFactory
import io.spinor.ig.api.rest.SessionService
import io.spinor.ig.api.shared.ConversationContext
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.scalatest.{FlatSpec, Matchers}

/**
  * Tests for the IG Index streaming service.
  *
  * @author A. Mahmood (arshadm@spinor.io)
  */
class StreamingServiceSpec extends FlatSpec with Matchers {
  /** Load the test configuration. */
  val config = ConfigFactory.load("test")

  /** The API key. */
  val apiKey = config.getString("ig.api.key")

  /** The API username. */
  val username = config.getString("ig.api.username")

  /** The API password. */
  val password = config.getString("ig.api.password")

  /** The connection manager. */
  val connectionManager = new PoolingHttpClientConnectionManager()

  /** The http client instance. */
  val httpClient = HttpClients.custom().setConnectionManager(connectionManager).build()

  /** The session service. */
  val sessionService = new SessionService(config, httpClient)

  /** The streaming service. */
  val streamingService = new StreamingService()

  "Connecting to IG streaming service" should "succeed" in {
    val basicConversationContext = ConversationContext(None, None, apiKey)

    val (sessionContext, sessionResponse) = sessionService.login(basicConversationContext, username, password)

    streamingService.connect(username, sessionContext, sessionResponse.getLightstreamerEndpoint())

    val adapter = new HandyTableListenerAdapter()
    streamingService.subscribeForMarket("IX.D.FTSE.DAILY.IP", adapter, Array("UPDATE_TIME", "BID", "OFFER"))

    // wait for 30 seconds
    Thread.sleep(60000L)

    // close the connection
    streamingService.disconnect()
  }
}
