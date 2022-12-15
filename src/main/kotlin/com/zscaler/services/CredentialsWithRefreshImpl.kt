package com.zscaler.services

import com.intellij.collaboration.auth.credentials.CredentialsWithRefresh

class CredentialsWithRefreshImpl(
    override val accessToken: String,
    override val expiresIn: Long,
    override val refreshToken: String,
    private val lastRefresh: Long,
) : CredentialsWithRefresh {
    override fun isAccessTokenValid(): Boolean {
        return System.currentTimeMillis() - lastRefresh < expiresIn
    }
}