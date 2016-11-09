package io.spinor.ig.api.streaming

import com.lightstreamer.ls_client.{ConnectionListener, PushConnException, PushServerException}
import org.slf4j.LoggerFactory

class ConnectionListenerAdapter extends ConnectionListener {

  private val logger = LoggerFactory.getLogger(classOf[ConnectionListenerAdapter])

  override def onConnectionEstablished(): Unit = {
    logger.debug("onConnectionEstablished")
  }

  override def onSessionStarted(b: Boolean): Unit = {
    logger.debug("onSessionStarted " + b)
  }

  override def onNewBytes(l: Long): Unit = {
    logger.debug("onNewBytes " + l)
  }

  override def onDataError(ex: PushServerException): Unit = {
    logger.debug("onDataError ", ex)
  }

  override def onActivityWarning(b: Boolean): Unit = {
    logger.debug("onActivityWarning")
  }

  override def onClose(): Unit = {
    logger.debug("onClose")
  }

  override def onEnd(i: Int): Unit = {
    logger.debug("onEnd " + i)
  }

  override def onFailure(ex: PushServerException): Unit = {
    logger.debug("onFailure", ex)
    throw new RuntimeException("onFailure " + ex)
  }

  override def onFailure(ex: PushConnException): Unit = {
    logger.debug("onFailure", ex)
    throw new RuntimeException("onFailure " + ex)
  }

}
