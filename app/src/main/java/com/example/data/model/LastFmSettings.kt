package com.example.data.model

data class LastFmSettings(
    val apiKey: String = "",
    val apiSecret: String = "",
    val username: String = "",
    val sessionKey: String = "",
    val authToken: String = "",
    val enabled: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = apiKey.isNotBlank() && apiSecret.isNotBlank() && sessionKey.isNotBlank()
}
