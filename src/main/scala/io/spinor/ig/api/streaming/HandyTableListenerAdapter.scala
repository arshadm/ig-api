package io.spinor.ig.api.streaming

import com.lightstreamer.ls_client.{HandyTableListener, SubscribedTableKey, UpdateInfo}
import org.slf4j.LoggerFactory

class HandyTableListenerAdapter extends HandyTableListener {
  private val logger = LoggerFactory.getLogger(classOf[HandyTableListenerAdapter])

  private var subscribedTableKey: SubscribedTableKey = null

  override def onUpdate(i: Int, s: String, updateInfo: UpdateInfo): Unit = {
    logger.info(s"onUpdate i: $i s: $s, updateInfo: $updateInfo")
  }

  override def onSnapshotEnd(i: Int, s: String): Unit = {
    logger.info(s"onSnapshotEnd i: $i, s: $s")
  }

  override def onRawUpdatesLost(i: Int, s: String, i2: Int): Unit = {
    logger.info(s"onRawUpdatesLost i: $i, s: $s, i2: $i2")
  }

  override def onUnsubscr(i: Int, s: String): Unit = {
    logger.info(s"onUnsubscr i: $i, s: $s")
  }

  override def onUnsubscrAll(): Unit = {
    logger.info("onUnsubscrAll")
  }

  def getSubscribedTableKey(): SubscribedTableKey = {
    return subscribedTableKey
  }

  def setSubscribedTableKey(subscribedTableKey: SubscribedTableKey): Unit = {
    this.subscribedTableKey = subscribedTableKey
  }
}
