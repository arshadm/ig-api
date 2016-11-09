package io.spinor.ig.api.streaming

import com.lightstreamer.ls_client._
import io.spinor.ig.api.shared.ConversationContext

class StreamingService {

  private val TRADE_PATTERN = "TRADE:{accountId}"
  private val ACCOUNT_BALANCE_INFO_PATTERN = "ACCOUNT:{accountId}"
  private val MARKET_L1_PATTERN = "MARKET:{epic}"
  private val SPRINT_MARKET_PATTERN = "MARKET:{epic}"
  private val CHART_TICK_PATTERN = "CHART:{epic}:TICK"
  private val CHART_CANDLE_PATTERN = "CHART:{epic}:{scale}"

  private var lsClient: LSClient = null

  abstract class PasswordStrategy(token: String) {
    def get(conversationContext: ConversationContext) = {
      val password = new StringBuilder()

      conversationContext.clientSecurityToken.map(password.append(token).append(_))
      conversationContext.accountSecurityToken.map(password.append("|XST-").append(_))

      password.toString()
    }
  }

  sealed case class ClientPasswordStrategy() extends PasswordStrategy("CST-")

  sealed case class VendorPasswordStratgey() extends PasswordStrategy("B2B-")

  def connect(username: String, conversationContext: ConversationContext, lightstreamerEndpoint: String): ConnectionListener = {
    connect(username, conversationContext, ClientPasswordStrategy(), lightstreamerEndpoint)
  }

  def connectVendor(username: String, conversationContext: ConversationContext,
                    lightstreamerEndpoint: String): ConnectionListener = {
    connect(username, conversationContext, VendorPasswordStratgey(), lightstreamerEndpoint)
  }

  private def connect(username: String, conversationContext: ConversationContext, passwordStrategy: PasswordStrategy,
                      lightstreamerEndpoint: String): ConnectionListener = {

    lsClient = new LSClient()
    val connectionInfo = new ConnectionInfo()
    connectionInfo.user = username
    connectionInfo.password = passwordStrategy.get(conversationContext)
    connectionInfo.pushServerUrl = lightstreamerEndpoint

    val adapter = new ConnectionListenerAdapter()

    lsClient.openConnection(connectionInfo, adapter)

    adapter
  }

  def disconnect(): Unit = {
    if (lsClient != null) {
      lsClient.closeConnection()
    }
  }

  def unsubscribe(key: SubscribedTableKey): Unit = {
    if (lsClient != null) {
      lsClient.forceUnsubscribeTable(key)
    }
  }

  def subscribe(adapter: HandyTableListenerAdapter, items: Array[String], mode: String, fields: Array[String]): HandyTableListenerAdapter = {
    val extendedTableInfo = new ExtendedTableInfo(items, mode, fields, false)
    adapter.setSubscribedTableKey(lsClient.subscribeTable(extendedTableInfo, adapter, false))
    adapter
  }

  def subscribeForConfirms(accountId: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val tradeKey = TRADE_PATTERN.replace("{accountId}", accountId)

    val extendedTableInfo = new ExtendedTableInfo(Array(tradeKey), "DISTINCT", Array("CONFIRMS"), false)

    val subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForAccountBalanceInfo(accountId: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = ACCOUNT_BALANCE_INFO_PATTERN.replace("{accountId}", accountId)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE",
      Array("PNL", "DEPOSIT", "USED_MARGIN", "AMOUNT_DUE", "AVAILABLE_CASH"), true)

    val subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)
    adapter
  }

  def subscribeForMarket(epic: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = MARKET_L1_PATTERN.replace("{epic}", epic)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE",
      Array("BID", "OFFER", "MARKET_STATE"), true)

    val subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)
    adapter
  }

  def subscribeForMarket(epic: String, adapter: HandyTableListenerAdapter, fields: Array[String]): HandyTableListenerAdapter = {
    val subscriptionKey = MARKET_L1_PATTERN.replace("{epic}", epic)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE", fields, true)

    val subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForB2CSprintMarket(epic: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = SPRINT_MARKET_PATTERN.replace("{epic}", epic)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE",
      Array("STRIKE_PRICE", "MARKET_STATE", "ODDS"), true)

    val subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForB2BSprintMarket(epic: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = SPRINT_MARKET_PATTERN.replace("{epic}", epic)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE",
      Array("STRIKE_PRICE", "SETTLEMENT_PRICE", "MARKET_STATE", "ODDS"), true)

    val subscribedTableKey = lsClient.subscribeTable(
      extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }


  def subscribeForOPUs(accountId: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val tradeKey = TRADE_PATTERN.replace("{accountId}", accountId)

    val extendedTableInfo = new ExtendedTableInfo(Array(tradeKey), "DISTINCT", Array("OPU"), false)

    val subscribedTableKey = lsClient.subscribeTable(
      extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForWOUs(accountId: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val tradeKey = TRADE_PATTERN.replace("{accountId}", accountId)

    val extendedTableInfo = new ExtendedTableInfo(Array(tradeKey), "DISTINCT", Array("WOU"), false)

    val subscribedTableKey = lsClient.subscribeTable(
      extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForChartTicks(epic: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = CHART_TICK_PATTERN.replace("{epic}", epic)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "DISTINCT",
      Array("BID", "OFR", "LTP", "LTV", "UTM", "DAY_OPEN_MID", "DAY_PERC_CHG_MID", "DAY_HIGH", "DAY_LOW"), true)

    val subscribedTableKey = lsClient.subscribeTable(
      extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

  def subscribeForChartCandles(epic: String, scale: String, adapter: HandyTableListenerAdapter): HandyTableListenerAdapter = {
    val subscriptionKey = CHART_CANDLE_PATTERN.replace("{epic}", epic)
    subscriptionKey.replace("{scale}", scale)

    val extendedTableInfo = new ExtendedTableInfo(Array(subscriptionKey), "MERGE", Array("LTV", "LTV", "UTM",
      "DAY_OPEN_MID", "UTM", "DAY_OPEN_MID", "DAY_PERC_CHG_MID", "DAY_HIGH", "DAY_LOW", "OFR_OPEN",
      "OFR_HIGH", "OFR_LOW", "OFR_CLOSE", "BID_OPEN", "BID_HIGH", "BID_LOW", "BID_CLOSE", "LTP_OPEN",
      "LTP_HIGH", "LTP_LOW", "LTP_CLOSE", "CANDLE_START", "CANDLE_TICK_COUNT"), true)

    val subscribedTableKey = lsClient.subscribeTable(
      extendedTableInfo, adapter, false)
    adapter.setSubscribedTableKey(subscribedTableKey)

    adapter
  }

}
