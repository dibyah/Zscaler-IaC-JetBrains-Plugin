package com.zscaler.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.intellij.collaboration.auth.credentials.Credentials
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.util.Url
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AuthCredentialsAcquirer(
    private val authorizationCodeUrl: Url,
    private val tokenUrl: Url,
    private val clientId: String,

) : OAuthCredentialsAcquirer<CredentialsWithRefreshImpl> {
    override fun acquireCredentials(code: String): OAuthCredentialsAcquirer.AcquireCredentialsResult<CredentialsWithRefreshImpl> =
        CustomOAuthCredentialsAcquirerHttp.requestToken(tokenUrl, code, clientId, authorizationCodeUrl) { body, headers ->
            // TODO: extract token from body
            val tokenResponse = Gson().fromJson(body, TokenResponse::class.java)
            println(tokenResponse)
            CredentialsWithRefreshImpl(tokenResponse.access_token.toString(), tokenResponse.expires_in.toString().toLong(), tokenResponse.refresh_token.toString(), System.currentTimeMillis())
            //SimpleCredentials(tokenResponse.access_token.toString())
        }

    private fun getTokenUrlWithParameters(code: String) = tokenUrl.addParameters(
        mapOf(
            "client_id" to clientId,
            "code" to code,
            "grant_type" to "authorization_code",
            "redirect_uri" to authorizationCodeUrl.toExternalForm(),
        )
    )
}

object CustomOAuthCredentialsAcquirerHttp {
    fun <T : Credentials> requestToken(
        url: Url,
        code: String,
        clientId: String,
        authorizationCodeUrl: Url,
        credentialsProvider: (body: String, headers: HttpHeaders) -> T
    ): OAuthCredentialsAcquirer.AcquireCredentialsResult<T> {
        val response = try {
            requestToken(url, code, clientId, authorizationCodeUrl)
        }
        catch (e: IOException) {
            return OAuthCredentialsAcquirer.AcquireCredentialsResult.Error("Cannot exchange token: ${e.message}")
        }
        return convertToAcquireCredentialsResult(response) { body, headers ->
            credentialsProvider(body, headers)
        }
    }

    fun requestToken(url: Url, code: String, clientId: String, authorizationCodeUrl: Url): HttpResponse<String> {
        val tokenUrl = url.toExternalForm()
        val client = HttpClient.newHttpClient()
        val objectMapper = ObjectMapper()
        val requestBody = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(mapOf(
                "client_id" to clientId,
                "code" to code,
                "grant_type" to "authorization_code",
                "redirect_uri" to authorizationCodeUrl.toExternalForm(),
            ))

        val request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun <T : Credentials> convertToAcquireCredentialsResult(
        httpResponse: HttpResponse<String>,
        credentialsProvider: (body: String, headers: HttpHeaders) -> T
    ): OAuthCredentialsAcquirer.AcquireCredentialsResult<T> {
        return if (httpResponse.statusCode() == 200) {
            val creds = credentialsProvider(httpResponse.body(), httpResponse.headers())
            OAuthCredentialsAcquirer.AcquireCredentialsResult.Success(creds)
        }
        else {
            OAuthCredentialsAcquirer.AcquireCredentialsResult.Error(httpResponse.body().ifEmpty { "No token provided" })
        }
    }
}