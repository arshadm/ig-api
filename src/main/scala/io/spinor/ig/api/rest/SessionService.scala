package io.spinor.ig.api.rest

import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

import com.typesafe.config.Config
import io.spinor.ig.api.rest.dto.session.createSessionV2.{CreateSessionV2Request, CreateSessionV2Response}
import io.spinor.ig.api.rest.dto.session.encryptionKey.getEncryptionKeySessionV1.GetEncryptionKeySessionV1Response
import io.spinor.ig.api.rest.dto.session.updateActiveAccountV1.{UpdateActiveAccountV1Request, UpdateActiveAccountV1Response}
import io.spinor.ig.api.shared.ConversationContext
import org.apache.commons.codec.binary.Base64
import org.apache.http.client.HttpClient

/**
  * This class defines an implementation of the IG Index session service.
  *
  * @author A. Mahmood (arshadm@spinor.io)
  */
class SessionService(config: Config, httpClient: HttpClient) extends AbstractService(config, httpClient) {
  val DEFAULT_CHARSET = Charset.forName("UTF-8")
  val PKCS1_PADDING_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
  val RSA_ALGORITHM = "RSA"

  /**
    * Create a new session by logging into the system.
    *
    * @return the conversation context
    */
  def login(conversationContext: ConversationContext, username: String, password: String,
            encryptPassword: Boolean = true): (ConversationContext, CreateSessionV2Response) = {
    val wirePassword = if (encryptPassword) this.encryptPassword(conversationContext, password) else password

    val createSessionV2Request = new CreateSessionV2Request()
    createSessionV2Request.setIdentifier(username)
    createSessionV2Request.setPassword(wirePassword)
    createSessionV2Request.setEncryptedPassword(encryptPassword)

    val response = super.executeHttpPost(conversationContext, "2", "/session", Map(), createSessionV2Request)

    val sessionReponse = objectMapper.readValue(response.getEntity().getContent(), classOf[CreateSessionV2Response])

    val cstHeader = response.getHeaders("CST")(0)
    val astHeader = response.getHeaders("X-SECURITY-TOKEN")(0)

    val sessionContext = ConversationContext(Some(cstHeader.getValue()), Some(astHeader.getValue()), conversationContext.apiKey)

    (sessionContext, sessionReponse)
  }

  /**
    * Switch to a different account (can also set it as the default account).
    *
    * @param conversationContext the conversation context
    * @param accountId           the account to switch to
    * @param defaultAccount      flag to indicate if the default account should be switch to this account
    * @return the update active account response
    */
  def updateActiveAccount(conversationContext: ConversationContext, accountId: String,
                          defaultAccount: Boolean = false): UpdateActiveAccountV1Response = {
    val updateActiveAccountV1Request = new UpdateActiveAccountV1Request()
    updateActiveAccountV1Request.setAccountId(accountId)
    updateActiveAccountV1Request.setDefaultAccount(defaultAccount)

    val response = super.executeHttpPut(conversationContext, "1", "/session", Map(), updateActiveAccountV1Request)
    objectMapper.readValue(response.getEntity().getContent(), classOf[UpdateActiveAccountV1Response])
  }

  /**
    * Logout from the session.
    *
    * @param conversationContext the conversation context
    */
  def logout(conversationContext: ConversationContext): Int = {
    val response = super.executeHttpDelete(conversationContext, "1", "/session", Map())

    response.getStatusLine().getStatusCode()
  }

  /**
    * Gets the encryption key for subsequent session login with an encrypted password.
    *
    * @return the encryption key session response
    */
  def encryptionKey(conversationContext: ConversationContext): GetEncryptionKeySessionV1Response = {
    val response = super.executeHttpGet(conversationContext, "1", "/session/encryptionKey")

    objectMapper.readValue(response.getEntity().getContent(), classOf[GetEncryptionKeySessionV1Response])
  }

  /**
    * Encrypt the password with the specified encryption key and timestamp.
    *
    * @param password the password to be encrypted
    * @return the encrypted password
    */
  private def encryptPassword(conversationContext: ConversationContext, password: String): String = {
    val encryptionKeyResponse = this.encryptionKey(conversationContext)

    var input = stringToBytes(password + "|" + encryptionKeyResponse.getTimeStamp())
    input = Base64.encodeBase64(input)

    val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
    val keySpec = new X509EncodedKeySpec(Base64.decodeBase64(stringToBytes(encryptionKeyResponse.getEncryptionKey)))
    val publicKey = keyFactory.generatePublic(keySpec)

    val cipher = Cipher.getInstance(PKCS1_PADDING_TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    bytesToString(Base64.encodeBase64(cipher.doFinal(input)))
  }

  /**
    * Convert a string to raw bytes using the UTF-8 encoding.
    *
    * @param string the string
    * @return the raw bytes
    */
  private def stringToBytes(string: String): Array[Byte] = {
    string.getBytes(DEFAULT_CHARSET)
  }

  /**
    * Convert bytes back to a string using the UTF-8 encoding.
    *
    * @param bytes the raw bytes
    * @return the string
    */
  private def bytesToString(bytes: Array[Byte]): String = {
    new String(bytes, DEFAULT_CHARSET)
  }
}
