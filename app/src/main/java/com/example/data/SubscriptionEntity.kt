package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val category: String, // e.g. "Entertainment", "Music", etc.
    val amount: Double,
    val currency: String = "USD",
    val billingCycle: String, // "weekly", "monthly", "quarterly", "yearly"
    val startDate: Long,
    val nextRenewalDate: Long,
    val color: String = "#6C63FF",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
