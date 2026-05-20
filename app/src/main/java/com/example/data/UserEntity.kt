package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val passwordHash: String,
    val planTier: String = "free", // "free" or "premium"
    val planExpiresAt: Long? = null,
    val pushToken: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
