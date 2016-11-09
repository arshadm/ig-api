package io.spinor.ig.api.rest

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.Config
import io.spinor.ig.api.shared.{ConversationContext, HttpException}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.{HttpRequest, HttpResponse}
import org.slf4j.LoggerFactory

abstract class AbstractService(config: Config, httpClient: HttpClient) {
  /** The logger. */
  private val logger = LoggerFactory.getLogger(classOf[AbstractService])

  /** The IG Index application key. */
  private val IG_APPLICATION_KEY = "X-IG-API-KEY"

  /** The IG Index client SSO token name. */
  private val IG_CLIENT_SSO_TOKEN_NAME = "CST"

  /** The IG Index account SSO token name. */
  private val IG_ACCOUNT_SSO_TOKEN_NAME = "X-SECURITY-TOKEN"

  /** The IG Index API version. */
  private val IG_VERSION = "version"

  /** The IG Index method. */
  private val IG_METHOD = "_method"

  /** The ID Index dark cluster query parameter name. */
  private val IG_DARK_CLUSTER = "dark_cluster"

  /** The IG Index API domain url. */
  private val igApiDomainURL = config.getString("ig.api.domain.url")

  /** The IG Index flag to indicate whether to use the dark cluster. */
  private val igApiDarkCluster = config.getBoolean("ig.api.dark.cluster")

  /** The object mapper for conversion from/to JSON. */
  protected val objectMapper: ObjectMapper = new ObjectMapper()

  /**
    * Execute a [[HttpGet]] for the specified uri and url parameters.
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param uri                 the request uri
    * @param uriParams           the uri parameters
    * @param raiseException      raise an exception on failure
    * @return the http response
    */
  protected def executeHttpGet(conversationContext: ConversationContext, version: String, uri: String,
                               uriParams: Map[String, Option[String]] = Map(),
                               raiseException: Boolean = true): HttpResponse = {
    val httpGet = new HttpGet(generateUri(uri, uriParams))

    executeRequest(conversationContext, version, httpGet, raiseException)
  }

  /**
    * Execute a [[HttpPost]] with the specified parameters.
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param uri                 the uri
    * @param uriParams           the uri parameters
    * @param requestEntity       the request entity
    * @param raiseException      flag to indicate if an exception should be raised on error
    * @return the server response
    */
  protected def executeHttpPost(conversationContext: ConversationContext, version: String, uri: String,
                                uriParams: Map[String, Option[String]] = Map(), requestEntity: AnyRef,
                                raiseException: Boolean = true): HttpResponse = {
    val entityWriter = new StringWriter()
    objectMapper.writeValue(entityWriter, requestEntity)

    val httpPost = new HttpPost(generateUri(uri, uriParams))
    httpPost.setEntity(new StringEntity(entityWriter.toString(), ContentType.APPLICATION_JSON))

    executeRequest(conversationContext, version, httpPost, raiseException)
  }

  /**
    * Execute a [[HttpPut]] with the specified parameters.
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param uri                 the uri
    * @param uriParams           the uri parameters
    * @param requestEntity       the request entity
    * @param raiseException      flag to indicate if an exception should be raised on error
    * @return the server response
    */
  protected def executeHttpPut(conversationContext: ConversationContext, version: String, uri: String,
                                uriParams: Map[String, Option[String]] = Map(), requestEntity: AnyRef,
                                raiseException: Boolean = true): HttpResponse = {
    val entityWriter = new StringWriter()
    objectMapper.writeValue(entityWriter, requestEntity)

    val httpPut = new HttpPut(generateUri(uri, uriParams))
    httpPut.setEntity(new StringEntity(entityWriter.toString(), ContentType.APPLICATION_JSON))

    executeRequest(conversationContext, version, httpPut, raiseException)
  }

  /**
    * Execute a [[HttpDelete]] for the specified uri and url parameters.
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param uri                 the request uri
    * @param uriParams           the uri parameters
    * @param raiseException      raise an exception on failure
    * @return the http response
    */
  protected def executeHttpDelete(conversationContext: ConversationContext, version: String, uri: String,
                               uriParams: Map[String, Option[String]] = Map(),
                               raiseException: Boolean = true): HttpResponse = {
    val httpDelete = new HttpDelete(generateUri(uri, uriParams))

    executeRequest(conversationContext, version, httpDelete, raiseException)
  }

  /**
    * Execute the request and check and raise an exception if an error occurs if requested.
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param request             the request
    * @param raiseException      flag to indicate if an exception should be raised on error
    * @return the server response
    */
  private def executeRequest(conversationContext: ConversationContext, version: String, request: HttpRequestBase,
                             raiseException: Boolean): HttpResponse = {
    addRequiredHeaders(conversationContext, version, request)

    val response = httpClient.execute(request)

    if (raiseException && (response.getStatusLine().getStatusCode() >= 300)) {
      throw new HttpException(response.getStatusLine.getStatusCode(), response.getStatusLine().getReasonPhrase())
    }

    response
  }

  /**
    * Add any required the headers on the [[HttpRequest]].
    *
    * @param conversationContext the conversation context
    * @param version             the API version
    * @param request             the request
    */
  private def addRequiredHeaders(conversationContext: ConversationContext, version: String, request: HttpRequestBase): HttpRequestBase = {

    conversationContext.accountSecurityToken.map(request.addHeader(IG_ACCOUNT_SSO_TOKEN_NAME, _))
    conversationContext.clientSecurityToken.map(request.addHeader(IG_CLIENT_SSO_TOKEN_NAME, _))

    request.addHeader(IG_VERSION, version)
    request.addHeader(IG_APPLICATION_KEY, conversationContext.apiKey)

    request
  }

  /**
    * Generate a new uri from the existing uri.
    *
    * @param uri       the initial uri
    * @param uriParams the uri params
    * @return the uri with all parameters replaced
    */
  private def generateUri(uri: String, uriParams: Map[String, Option[String]]): String = {
    var newUri = uri

    for ((name, value) <- uriParams) {
      if (value.isDefined) {
        newUri = newUri.replace(name, value.get)
      }
    }

    igApiDomainURL + addIGApiLightDarkCluster(newUri)
  }

  /**
    * Append the appropriate query parameter for dark cluster is required.
    *
    * @param url the existing url
    * @return the enhanced url if dark cluster is required
    */
  private def addIGApiLightDarkCluster(url: String): String = {
    if (igApiDarkCluster) {
      url + (if (url.indexOf('?') >= 0) "&" else "?") + IG_DARK_CLUSTER + "=dark"
    } else {
      url
    }
  }
}
