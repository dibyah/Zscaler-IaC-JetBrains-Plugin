package com.zscaler.services

import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.openapi.components.service
import com.intellij.util.Url
import com.intellij.util.Urls
import com.intellij.util.io.DigestUtil
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.ide.RestService

class AuthRequest : OAuthRequest<CredentialsWithRefreshImpl> {

    private val clientId = "KmIDWAFvw4O4j1aK60PVCCzF741NckBT"
    private val authUrl = Urls.newFromEncoded("https://main.dev.app.zscwp.io/auth/cli")
    private val tokenUrl = Urls.newFromEncoded("https://dev-auth.zscwp.io/oauth/token")
    private val state get() = DigestUtil.randomToken()
    private val port by lazy { BuiltInServerManager.getInstance().port }

    override val authUrlWithParameters: Url
        get() = authUrl.addParameters(
            mapOf(
                "scope" to "offline_access openid profile email",
                "response_type" to "code",
                "state" to state,
                "redirect_uri" to authorizationCodeUrl.toString(),
                "client_id" to clientId,
            )
        )

    override val authorizationCodeUrl by lazy {
        Urls.newFromEncoded("http://localhost:$port/${RestService.PREFIX}/${service<AuthService>().name}")
    }

    override val credentialsAcquirer: AuthCredentialsAcquirer = AuthCredentialsAcquirer(authUrl, tokenUrl, clientId)
}