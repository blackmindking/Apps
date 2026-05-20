package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthManager(context: Context, private val db: AppDatabase) {
    private val prefs: SharedPreferences = context.getSharedPreferences("subtrackr_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _offlineQueueCount = MutableStateFlow(0)
    val offlineQueueCount: StateFlow<Int> = _offlineQueueCount.asStateFlow()

    fun getSavedUserId(): String? = prefs.getString("logged_in_user_id", null)

    suspend fun register(email: String, name: String, passwordHash: String): Result<UserEntity> {
        val existing = db.userDao().getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val userId = UUID.randomUUID().toString()
        val user = UserEntity(
            id = userId,
            email = email,
            name = name,
            passwordHash = passwordHash,
            planTier = "free",
            planExpiresAt = null,
            createdAt = System.currentTimeMillis()
        )

        db.userDao().insertUser(user)
        setLoggedInUser(user)
        return Result.success(user)
    }

    suspend fun login(email: String, passwordHash: String): Result<UserEntity> {
        val user = db.userDao().getUserByEmail(email) ?: return Result.failure(Exception("No account found with this email."))
        if (user.passwordHash != passwordHash) {
            return Result.failure(Exception("Incorrect password. Please try again."))
        }

        setLoggedInUser(user)
        return Result.success(user)
    }

    suspend fun resetPassword(email: String, newPasswordHash: String): Boolean {
        val user = db.userDao().getUserByEmail(email) ?: return false
        val updated = user.copy(passwordHash = newPasswordHash)
        db.userDao().insertUser(updated)
        if (_currentUser.value?.email == email) {
            _currentUser.value = updated
        }
        return true
    }

    fun logout() {
        prefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
    }

    suspend fun setPremium(isPremium: Boolean) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            planTier = if (isPremium) "premium" else "free",
            planExpiresAt = if (isPremium) System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) else null
        )
        db.userDao().insertUser(updated)
        setLoggedInUser(updated)
    }

    fun setLoggedInUser(user: UserEntity) {
        _currentUser.value = user
        prefs.edit().putString("logged_in_user_id", user.id).apply()
    }

    // Setting prefs helpers
    fun getDefaultCurrency(): String = prefs.getString("setting_default_currency", "USD") ?: "USD"
    fun setDefaultCurrency(curr: String) = prefs.edit().putString("setting_default_currency", curr).apply()

    fun getDefaultReminderDays(): Int = prefs.getInt("setting_default_reminder_days", 3)
    fun setDefaultReminderDays(days: Int) = prefs.edit().putInt("setting_default_reminder_days", days).apply()

    fun getTheme(): String = prefs.getString("setting_theme", "dark") ?: "dark"
    fun setTheme(theme: String) = prefs.edit().putString("setting_theme", theme).apply()

    fun getNotificationTime(): Pair<Int, Int> {
        val hour = prefs.getInt("setting_notification_hour", 8)
        val min = prefs.getInt("setting_notification_min", 0)
        return Pair(hour, min)
    }
    fun setNotificationTime(hour: Int, min: Int) {
        prefs.edit().putInt("setting_notification_hour", hour).putInt("setting_notification_min", min).apply()
    }

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("setting_notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean("setting_notifications_enabled", enabled).apply()

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        if (!_isOfflineMode.value) {
            _offlineQueueCount.value = 0 // Clear synced queue
        }
    }

    fun queueOfflineAction() {
        if (_isOfflineMode.value) {
            _offlineQueueCount.value += 1
        }
    }

    suspend fun deleteAccount() {
        val user = _currentUser.value ?: return
        db.subscriptionDao().clearUserSubscriptions(user.id)
        db.userDao().deleteUser(user)
        logout()
    }

    suspend fun clearAllSubscriptions() {
        val user = _currentUser.value ?: return
        db.subscriptionDao().clearUserSubscriptions(user.id)
    }
}
