package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettingsData(
    val isDriveConnected: Boolean = false,
    val driveAccountEmail: String = "",
    val driveRootFolder: String = "Union Parishad File Management",
    val googleAccessToken: String? = null,
    val googleRefreshToken: String? = null,
    val tokenExpiresAt: Long = 0L,
    val googleOAuthClientId: String = "",
    val upName: String = "১নং রামপুর ইউনিয়ন পরিষদ",
    val entrepreneurName: String = "মোঃ রুবেল হোসেন (উদ্যোক্তা)",
    val upazilaDistrict: String = "উপজেলা: সদর, জেলা: ফেনী",
    val userRole: String = "Admin / Entrepreneur",
    val autoUploadOnCapture: Boolean = false
) {
    val isTokenExpired: Boolean
        get() = tokenExpiresAt > 0L && System.currentTimeMillis() >= (tokenExpiresAt - 60_000L)
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("up_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettingsData> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettingsData {
        return AppSettingsData(
            isDriveConnected = prefs.getBoolean("drive_connected", false),
            driveAccountEmail = prefs.getString("drive_email", "") ?: "",
            driveRootFolder = prefs.getString("drive_root", "Union Parishad File Management") ?: "Union Parishad File Management",
            googleAccessToken = prefs.getString("google_access_token", null),
            googleRefreshToken = prefs.getString("google_refresh_token", null),
            tokenExpiresAt = prefs.getLong("token_expires_at", 0L),
            googleOAuthClientId = prefs.getString("oauth_client_id", "") ?: "",
            upName = prefs.getString("up_name", "১নং রামপুর ইউনিয়ন পরিষদ") ?: "১নং রামপুর ইউনিয়ন পরিষদ",
            entrepreneurName = prefs.getString("entrepreneur_name", "মোঃ রুবেল হোসেন (উদ্যোক্তা)") ?: "মোঃ রুবেল হোসেন (উদ্যোক্তা)",
            upazilaDistrict = prefs.getString("upazila_district", "উপজেলা: সদর, জেলা: ফেনী") ?: "উপজেলা: সদর, জেলা: ফেনী",
            userRole = prefs.getString("user_role", "Admin / Entrepreneur") ?: "Admin / Entrepreneur",
            autoUploadOnCapture = prefs.getBoolean("auto_upload", false)
        )
    }

    fun updateDriveConnection(
        connected: Boolean,
        email: String = "",
        accessToken: String? = null,
        refreshToken: String? = null,
        expiresInSeconds: Long = 3600L
    ) {
        val expiresAt = if (connected && accessToken != null) {
            System.currentTimeMillis() + (expiresInSeconds * 1000L)
        } else {
            0L
        }

        prefs.edit()
            .putBoolean("drive_connected", connected)
            .putString("drive_email", email)
            .putString("google_access_token", accessToken)
            .putString("google_refresh_token", refreshToken)
            .putLong("token_expires_at", expiresAt)
            .apply()

        _settingsFlow.value = _settingsFlow.value.copy(
            isDriveConnected = connected,
            driveAccountEmail = email,
            googleAccessToken = accessToken,
            googleRefreshToken = refreshToken,
            tokenExpiresAt = expiresAt
        )
    }

    fun updateOAuthClientId(clientId: String) {
        prefs.edit().putString("oauth_client_id", clientId.trim()).apply()
        _settingsFlow.value = _settingsFlow.value.copy(googleOAuthClientId = clientId.trim())
    }

    fun disconnectDrive() {
        prefs.edit()
            .putBoolean("drive_connected", false)
            .putString("drive_email", "")
            .remove("google_access_token")
            .remove("google_refresh_token")
            .remove("token_expires_at")
            .apply()

        _settingsFlow.value = _settingsFlow.value.copy(
            isDriveConnected = false,
            driveAccountEmail = "",
            googleAccessToken = null,
            googleRefreshToken = null,
            tokenExpiresAt = 0L
        )
    }

    fun updateProfile(upName: String, entrepreneurName: String, upazilaDistrict: String) {
        prefs.edit()
            .putString("up_name", upName)
            .putString("entrepreneur_name", entrepreneurName)
            .putString("upazila_district", upazilaDistrict)
            .apply()
        _settingsFlow.value = _settingsFlow.value.copy(
            upName = upName,
            entrepreneurName = entrepreneurName,
            upazilaDistrict = upazilaDistrict
        )
    }

    fun updateAutoUpload(enabled: Boolean) {
        prefs.edit().putBoolean("auto_upload", enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoUploadOnCapture = enabled)
    }
}
