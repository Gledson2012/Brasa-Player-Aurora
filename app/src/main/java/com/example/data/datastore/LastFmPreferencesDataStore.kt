package com.example.data.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.LastFmSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

val Context.lastFmDataStore by preferencesDataStore(name = "lastfm_preferences")

class LastFmPreferencesDataStore(private val context: Context) {
    private object Keys {
        val API_KEY = stringPreferencesKey("api_key")
        val API_SECRET = stringPreferencesKey("api_secret")
        val USERNAME = stringPreferencesKey("username")
        val SESSION_KEY = stringPreferencesKey("session_key")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val ENABLED = booleanPreferencesKey("enabled")
    }

    val settingsFlow: Flow<LastFmSettings> = context.lastFmDataStore.data.map { preferences ->
        LastFmSettings(
            apiKey = LastFmSecrets.reveal(preferences[Keys.API_KEY]),
            apiSecret = LastFmSecrets.reveal(preferences[Keys.API_SECRET]),
            username = LastFmSecrets.reveal(preferences[Keys.USERNAME]),
            sessionKey = LastFmSecrets.reveal(preferences[Keys.SESSION_KEY]),
            authToken = LastFmSecrets.reveal(preferences[Keys.AUTH_TOKEN]),
            enabled = preferences[Keys.ENABLED] ?: false
        )
    }

    suspend fun saveCredentials(apiKey: String, apiSecret: String) {
        context.lastFmDataStore.edit { preferences ->
            preferences[Keys.API_KEY] = LastFmSecrets.protect(apiKey.trim())
            preferences[Keys.API_SECRET] = LastFmSecrets.protect(apiSecret.trim())
            preferences.remove(Keys.AUTH_TOKEN)
            preferences.remove(Keys.SESSION_KEY)
            preferences.remove(Keys.USERNAME)
            preferences[Keys.ENABLED] = false
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.lastFmDataStore.edit { preferences ->
            preferences[Keys.AUTH_TOKEN] = LastFmSecrets.protect(token)
        }
    }

    suspend fun saveSession(username: String, sessionKey: String) {
        context.lastFmDataStore.edit { preferences ->
            preferences[Keys.USERNAME] = LastFmSecrets.protect(username)
            preferences[Keys.SESSION_KEY] = LastFmSecrets.protect(sessionKey)
            preferences[Keys.ENABLED] = true
        }
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.lastFmDataStore.edit { preferences ->
            encryptLegacyValues(preferences)
            preferences[Keys.ENABLED] = enabled
        }
    }

    suspend fun restore(settings: LastFmSettings) {
        context.lastFmDataStore.edit { preferences ->
            preferences[Keys.API_KEY] = LastFmSecrets.protect(settings.apiKey)
            preferences[Keys.API_SECRET] = LastFmSecrets.protect(settings.apiSecret)
            preferences[Keys.USERNAME] = LastFmSecrets.protect(settings.username)
            preferences[Keys.SESSION_KEY] = LastFmSecrets.protect(settings.sessionKey)
            preferences[Keys.AUTH_TOKEN] = LastFmSecrets.protect(settings.authToken)
            preferences[Keys.ENABLED] = settings.enabled
        }
    }

    suspend fun clear() {
        context.lastFmDataStore.edit { it.clear() }
    }

    private fun encryptLegacyValues(preferences: MutablePreferences) {
        listOf(Keys.API_KEY, Keys.API_SECRET, Keys.USERNAME, Keys.SESSION_KEY, Keys.AUTH_TOKEN)
            .forEach { key ->
                val value = preferences[key]
                if (!value.isNullOrBlank() && !value.startsWith("enc:v1:")) {
                    preferences[key] = LastFmSecrets.protect(value)
                }
            }
    }
}

private object LastFmSecrets {
    private const val KEY_ALIAS = "lastfm_credentials_key"
    private const val PREFIX = "enc:v1:"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH_BYTES = 12

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    fun protect(value: String): String {
        if (value.isBlank()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = ByteBuffer.allocate(cipher.iv.size + encrypted.size)
        payload.put(cipher.iv)
        payload.put(encrypted)
        return PREFIX + Base64.encodeToString(payload.array(), Base64.NO_WRAP)
    }

    fun reveal(value: String?): String {
        if (value.isNullOrBlank()) return ""
        if (!value.startsWith(PREFIX)) return value // migrate legacy plaintext on next write

        return runCatching {
            val payload = Base64.decode(value.removePrefix(PREFIX), Base64.NO_WRAP)
            val iv = payload.copyOfRange(0, IV_LENGTH_BYTES)
            val encrypted = payload.copyOfRange(IV_LENGTH_BYTES, payload.size)
            Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            }.doFinal(encrypted).toString(Charsets.UTF_8)
        }.getOrDefault("")
    }
}
