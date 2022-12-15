package com.zscaler.services

import com.intellij.collaboration.auth.services.OAuthServiceBase
import com.intellij.openapi.components.Service

@Service
internal class AuthService : OAuthServiceBase<CredentialsWithRefreshImpl>() {
    override val name: String
        get() = ""

    override fun revokeToken(token: String) {
        TODO("Not yet implemented")
    }
}