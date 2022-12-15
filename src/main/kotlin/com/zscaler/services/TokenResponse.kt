package com.zscaler.services

class TokenResponse {
    var access_token: String? = null
    var refresh_token: String? = null
    var id_token: String? = null
    var scope: String? = null
    var expires_in: Long? = null
    var token_type: String? = null

    override fun toString(): String {
        return "TokenResponse(access_token=$access_token, refresh_token=$refresh_token, id_token=$id_token, scope=$scope, expires_in=$expires_in, token_type=$token_type)"
    }
}