package com.twin.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Prefs(context: Context) {
    // Keystore-backed prefs fail on some devices/ROMs; fall back to plain prefs
    // rather than crashing the whole app at startup.
    private val sp: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "twin_secure",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        context.getSharedPreferences("twin_plain", Context.MODE_PRIVATE)
    }

    var serverUrl: String
        get() = sp.getString("server_url", "https://twin.bconclub.com") ?: ""
        set(v) = sp.edit().putString("server_url", v.trim().trimEnd('/')).apply()

    var token: String
        get() = sp.getString("token", "") ?: ""
        set(v) = sp.edit().putString("token", v.trim()).apply()

    var interviewed: Boolean
        get() = sp.getBoolean("interviewed", false)
        set(v) = sp.edit().putBoolean("interviewed", v).apply()

    var lastTwinMessage: String
        get() = sp.getString("last_twin_message", "") ?: ""
        set(v) = sp.edit().putString("last_twin_message", v.take(200)).apply()

    val configured: Boolean get() = serverUrl.isNotBlank() && token.isNotBlank()
}
