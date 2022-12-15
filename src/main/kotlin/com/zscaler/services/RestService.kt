package com.zscaler.services

import com.intellij.collaboration.auth.OAuthCallbackHandlerBase
import com.intellij.collaboration.auth.services.OAuthService
import com.intellij.openapi.components.service

class RestService : OAuthCallbackHandlerBase() {

    override fun handleAcceptCode(isAccepted: Boolean) = AcceptCodeHandleResult.Page("<strong>OK</strong>")

    override fun oauthService(): OAuthService<*> = service<AuthService>()
}