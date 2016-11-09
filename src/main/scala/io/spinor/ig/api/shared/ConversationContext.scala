package io.spinor.ig.api.shared

sealed case class ConversationContext(clientSecurityToken: Option[String], accountSecurityToken: Option[String], apiKey: String)
