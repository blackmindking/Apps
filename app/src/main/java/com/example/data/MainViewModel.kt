package com.example.data

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.theme.AppColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.UUID

sealed class Destination {
    object Welcome : Destination()
    object Login : Destination()
    object Register : Destination()
    object ForgotPassword : Destination()
    object Onboarding : Destination()
    object MainApp : Destination()
}

enum class Tab {
    Dashboard,
    Subscriptions,
    Add,
    Analytics,
    Settings
}

data class ToastParams(val message: String, val isSuccess: Boolean = true, val isWifiOff: Boolean = false)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val authManager = AuthManager(application, db)

    // Navigation & Tabs
    private val _currentDestination = MutableStateFlow<Destination>(Destination.Welcome)
    val currentDestination: StateFlow<Destination> = _currentDestination.asStateFlow()

    private val _currentTab = MutableStateFlow(Tab.Dashboard)
    val currentTab: StateFlow<Tab> = _currentTab.asStateFlow()

    // CRUD state
    private val _editingSubscriptionId = MutableStateFlow<String?>(null)
    val editingSubscriptionId: StateFlow<String?> = _editingSubscriptionId.asStateFlow()

    // Subscriptions filtering state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("nextRenewalDate") // "name", "amount", "nextRenewalDate", "recent"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // Toasts overlay state
    private val _toast = MutableStateFlow<ToastParams?>(null)
    val toast: StateFlow<ToastParams?> = _toast.asStateFlow()

    // Paywall view state
    private val _isPaywallOpen = MutableStateFlow(false)
    val isPaywallOpen: StateFlow<Boolean> = _isPaywallOpen.asStateFlow()

    // Filtered subscriptions flow
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subscriptions: StateFlow<List<SubscriptionEntity>> = authManager.currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                db.subscriptionDao().getSubscriptionsFlow(user.id)
            }
        }
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .combine(_selectedCategoryFilter) { list, cat ->
            if (cat == "All") list else list.filter { it.category.equals(cat, ignoreCase = true) }
        }
        .combine(_sortBy) { list, sortKey ->
            when (sortKey) {
                "name" -> list.sortedBy { it.name.lowercase() }
                "amount" -> list.sortedBy { it.amount }
                "recent" -> list.sortedByDescending { it.createdAt }
                else -> list.sortedBy { it.nextRenewalDate }
            }
        }
        .combine(_sortAscending) { list, asc ->
            if (asc) list else list.reversed()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Hydrate session if user was previously logged in
        val savedId = authManager.getSavedUserId()
        if (savedId != null) {
            viewModelScope.launch {
                val user = db.userDao().getUserById(savedId)
                if (user != null) {
                    authManager.setLoggedInUser(user)
                    _currentDestination.value = Destination.MainApp
                } else {
                    _currentDestination.value = Destination.Welcome
                }
            }
        }

        // Observe auth changes to auto-gate screen destination
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user == null) {
                    _currentDestination.value = Destination.Welcome
                } else {
                    // Check if onboarding completed (simulate check)
                    _currentDestination.value = Destination.MainApp
                }
            }
        }
    }

    fun navigateTo(destination: Destination) {
        _currentDestination.value = destination
    }

    fun selectTab(tab: Tab) {
        _currentTab.value = tab
        if (tab != Tab.Add) {
            _editingSubscriptionId.value = null // reset edit selection
        }
    }

    fun startEditingSubscription(subId: String) {
        _editingSubscriptionId.value = subId
        _currentTab.value = Tab.Add
    }

    fun setFilters(query: String, category: String, sort: String, ascending: Boolean) {
        _searchQuery.value = query
        _selectedCategoryFilter.value = category
        _sortBy.value = sort
        _sortAscending.value = ascending
    }

    fun showToast(message: String, isSuccess: Boolean = true, isWifiOff: Boolean = false) {
        _toast.value = ToastParams(message, isSuccess, isWifiOff)
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            if (_toast.value?.message == message) {
                _toast.value = null
            }
        }
    }

    fun dismissToast() {
        _toast.value = null
    }

    fun openPaywall() {
        _isPaywallOpen.value = true
    }

    fun closePaywall() {
        _isPaywallOpen.value = false
    }

    // CRUD operations
    fun saveSubscription(
        name: String,
        category: String,
        amount: Double,
        currency: String,
        billingCycle: String,
        startDateMs: Long,
        color: String,
        notes: String,
        reminderEnabled: Boolean,
        reminderDaysBefore: Int,
        onComplete: () -> Unit
    ) {
        val user = authManager.currentUser.value ?: return

        // Free tier limit check (max 5)
        viewModelScope.launch {
            val existingSubs = db.subscriptionDao().getSubscriptions(user.id)
            val subId = _editingSubscriptionId.value

            if (user.planTier == "free" && existingSubs.size >= 5 && subId == null) {
                // Limit exceeded
                showToast("Limit exceeded! Upgrade to Premium for unlimited subscriptions.", isSuccess = false)
                openPaywall()
                return@launch
            }

            // Calculate next renewal date
            val nextRenewal = Calculations.getNextRenewalFromToday(startDateMs, billingCycle)

            val subscription = SubscriptionEntity(
                id = subId ?: UUID.randomUUID().toString(),
                userId = user.id,
                name = name,
                category = category,
                amount = amount,
                currency = currency,
                billingCycle = billingCycle,
                startDate = startDateMs,
                nextRenewalDate = nextRenewal,
                color = color,
                notes = notes,
                reminderEnabled = reminderEnabled,
                reminderDaysBefore = reminderDaysBefore,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (authManager.isOfflineMode.value) {
                authManager.queueOfflineAction()
                db.subscriptionDao().insertSubscription(subscription)
                showToast("Saved offline!", isSuccess = true, isWifiOff = true)
            } else {
                db.subscriptionDao().insertSubscription(subscription)
                showToast(if (subId == null) "Subscription added! ✨" else "Subscription updated! ✨")
            }

            _editingSubscriptionId.value = null
            selectTab(Tab.Dashboard)
            onComplete()
        }
    }

    fun toggleSubscriptionActive(sub: SubscriptionEntity) {
        viewModelScope.launch {
            val updated = sub.copy(
                isActive = !sub.isActive,
                updatedAt = System.currentTimeMillis()
            )
            db.subscriptionDao().updateSubscription(updated)
            showToast(if (updated.isActive) "Subscription activated 🟢" else "Subscription paused ⏸️")
        }
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            db.subscriptionDao().deleteSubscription(sub)
            showToast("Subscription deleted 🗑️")
            // If in Add screen editing this specific item, reset
            if (_editingSubscriptionId.value == sub.id) {
                _editingSubscriptionId.value = null
            }
        }
    }

    // Seeding sample data upon registration so that the dashboard is beautifully visualized
    fun seedSampleDataForUser(userId: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val day = 24L * 60L * 60L * 1000L

            val samples = listOf(
                SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "Netflix",
                    category = "Entertainment",
                    amount = 15.49,
                    currency = "USD",
                    billingCycle = "monthly",
                    startDate = now - (15 * day),
                    nextRenewalDate = Calculations.getNextRenewalFromToday(now - (15 * day), "monthly"),
                    color = "#E50914",
                    notes = "Premium tier Ultra HD streams with UHD audio.",
                    reminderEnabled = true,
                    reminderDaysBefore = 3
                ),
                SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "Spotify Premium",
                    category = "Music",
                    amount = 11.99,
                    currency = "USD",
                    billingCycle = "monthly",
                    startDate = now - (6 * day),
                    nextRenewalDate = Calculations.getNextRenewalFromToday(now - (6 * day), "monthly"),
                    color = "#1DB954",
                    notes = "Duo plan shared with spouse.",
                    reminderEnabled = true,
                    reminderDaysBefore = 1
                ),
                SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "Adobe Lightroom",
                    category = "Software",
                    amount = 9.99,
                    currency = "USD",
                    billingCycle = "monthly",
                    startDate = now - (28 * day),
                    nextRenewalDate = Calculations.getNextRenewalFromToday(now - (28 * day), "monthly"),
                    color = "#FF2E00",
                    notes = "Photography plan with 20GB cloud storage.",
                    reminderEnabled = true,
                    reminderDaysBefore = 3
                ),
                SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "Gold Gym",
                    category = "Fitness",
                    amount = 49.00,
                    currency = "USD",
                    billingCycle = "monthly",
                    startDate = now - (10 * day),
                    nextRenewalDate = Calculations.getNextRenewalFromToday(now - (10 * day), "monthly"),
                    color = "#00D2FF",
                    notes = "Local membership plus steam sauna access.",
                    reminderEnabled = true,
                    reminderDaysBefore = 7
                ),
                SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "ChatGPT Plus",
                    category = "Productivity",
                    amount = 20.00,
                    currency = "USD",
                    billingCycle = "monthly",
                    startDate = now - (4 * day),
                    nextRenewalDate = Calculations.getNextRenewalFromToday(now - (4 * day), "monthly"),
                    color = "#10A37F",
                    notes = "Work and coding assistant subscription.",
                    reminderEnabled = false,
                    reminderDaysBefore = 3
                )
            )

            for (s in samples) {
                db.subscriptionDao().insertSubscription(s)
            }
        }
    }

    // Export local subscriptions as beautiful CSV file via sharing
    fun exportSubscriptionsToCSV(context: Context) {
        val user = authManager.currentUser.value ?: return
        viewModelScope.launch {
            val subsList = db.subscriptionDao().getSubscriptions(user.id)
            if (subsList.isEmpty()) {
                showToast("No subscriptions to export yet", isSuccess = false)
                return@launch
            }

            try {
                val csvFile = File(context.cacheDir, "SubTrackr_Export.csv")
                csvFile.writeText(buildString {
                    append("Name,Category,Amount,Currency,Billing Cycle,Next Renewal,Notes,Is Active\n")
                    for (s in subsList) {
                        val nextDate = Calculations.formatDate(s.nextRenewalDate)
                        val escapedNotes = s.notes.replace("\"", "\"\"")
                        append("\"${s.name}\",\"${s.category}\",${s.amount},\"${s.currency}\",\"${s.billingCycle}\",\"$nextDate\",\"$escapedNotes\",${s.isActive}\n")
                    }
                })

                // Request simple platform share
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    csvFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Export Subscriptions CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                showToast("CSV file generated! 🗃️")
            } catch (e: Exception) {
                showToast("Failed to generate CSV: ${e.message}", isSuccess = false)
            }
        }
    }

    // Force pull subscription renewals recalculation logic
    fun syncNow() {
        val user = authManager.currentUser.value ?: return
        viewModelScope.launch {
            showToast("Syncing with cloud... 🔄")
            val list = db.subscriptionDao().getSubscriptions(user.id)
            var updatedCount = 0
            val now = System.currentTimeMillis()
            for (s in list) {
                if (s.isActive && s.nextRenewalDate < now) {
                    val nextRenewal = Calculations.getNextRenewalFromToday(s.startDate, s.billingCycle)
                    db.subscriptionDao().updateSubscription(s.copy(nextRenewalDate = nextRenewal, updatedAt = now))
                    updatedCount++
                }
            }
            kotlinx.coroutines.delay(1000)
            if (updatedCount > 0) {
                showToast("Sync finished! $updatedCount renewal(s) rolled over. ⏰")
            } else {
                showToast("Sync finished! Everything is up-to-date. ✨")
            }
        }
    }

    // Simulated test Push Notification
    fun sendTestPushNotification(context: Context) {
        viewModelScope.launch {
            showToast("Sending test push notification... ⏰")
            kotlinx.coroutines.delay(1000)
            NotificationHelper.showNotification(
                context,
                "⏰ SubTrackr Renewal Alert",
                "Your premium Spotify plan will renew in 3 days. USD $11.99 will be charged."
            )
        }
    }

    // Custom simulated transaction trigger for payments
    fun executePremiumPurchase(onPurchased: () -> Unit) {
        viewModelScope.launch {
            // Simulated transaction call
            kotlinx.coroutines.delay(1500)
            authManager.setPremium(true)
            showToast("Welcome to Premium! 👑 🎉")
            _isPaywallOpen.value = false
            onPurchased()
        }
    }
}
