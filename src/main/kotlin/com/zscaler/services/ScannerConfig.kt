package com.zscaler.services

import com.fasterxml.jackson.annotation.JsonProperty

class ScannerConfig {
    @JsonProperty("host")
    var host: String? = null

    @JsonProperty("appHost")
    var appHost: String? = null

    @JsonProperty("auth")
    var authConfig: AuthConfig? = null

    class AuthConfig {
        var host: String? = null
        var audience: String? = null
        var scope: String? = null
    }
}
