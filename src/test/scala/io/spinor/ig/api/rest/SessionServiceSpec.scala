package io.spinor.ig.api.rest

import com.typesafe.config.ConfigFactory
import io.spinor.ig.api.shared.ConversationContext
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.scalatest.{FlatSpec, Matchers}

/**
  * Tests for the IG Index session service.
  *
  * @author A. Mahmood (arshadm@spinor.io)
  */
class SessionServiceSpec extends FlatSpec with Matchers {
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

  "Calling encryption key" should "fetch a new encryption key" in {
    val conversationContext = ConversationContext(None, None, apiKey)

    val response = sessionService.encryptionKey(conversationContext)

    response.getEncryptionKey() should not be (null)
  }

  "Calling create session" should "create a new session" in {
    val conversationContext = ConversationContext(None, None, apiKey)

    val (sessionContext, sessionResponse) = sessionService.login(conversationContext, username, password)

    sessionResponse.getAccounts().get(0).getAccountId() should be ("XXB1U")
  }

  "Calling update account " should " update account" in {
    val conversationContext = ConversationContext(None, None, apiKey)

    val (sessionContext, sessionResponse) = sessionService.login(conversationContext, username, password)

    val response = sessionService.updateActiveAccount(sessionContext, "XXB1V")
    System.out.println(response)
  }

  "Calling logout" should " perform a logout" in {
    val basicConversationContext = ConversationContext(None, None, apiKey)

    val (sessionContext, sessionResponse) = sessionService.login(basicConversationContext, username, password)

    val responseStatus = sessionService.logout(sessionContext)

    responseStatus should be (204)
  }
}
