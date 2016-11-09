package io.spinor.ig.api.shared

/**
  * This exception is thrown if a http call ended in an error.
  *
  * @author A. Mahmood (arshadm@spinor.io)
  */
class HttpException(statusCode: Int, message: String) extends RuntimeException {
}
