package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Calculations {

    fun toMonthlyAmount(amount: Double, cycle: String): Double {
        return when (cycle.lowercase()) {
            "weekly" -> amount * 4.33
            "monthly" -> amount * 1.0
            "quarterly" -> amount / 3.0
            "yearly" -> amount / 12.0
            else -> amount
        }
    }

    fun calculateNextRenewal(fromDateMs: Long, cycle: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromDateMs }
        when (cycle.lowercase()) {
            "weekly" -> cal.add(Calendar.DAY_OF_YEAR, 7)
            "monthly" -> cal.add(Calendar.MONTH, 1)
            "quarterly" -> cal.add(Calendar.MONTH, 3)
            "yearly" -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun getNextRenewalFromToday(startDateMs: Long, cycle: String): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        var date = startDateMs
        // If start date is in the future, that is the next renewal date.
        if (date >= today) return date
        // Otherwise calculate future renewal
        while (date < today) {
            date = calculateNextRenewal(date, cycle)
        }
        return date
    }

    fun daysUntil(targetDateMs: Long): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val diff = targetDateMs - today
        if (diff <= 0) return 0
        return (diff / (86400000L)).toInt()
    }

    fun totalPaidToDate(amount: Double, startDateMs: Long, cycle: String): Double {
        val now = System.currentTimeMillis()
        var date = startDateMs
        if (date > now) return 0.0
        var count = 0
        while (date <= now) {
            count++
            date = calculateNextRenewal(date, cycle)
        }
        val timesCharged = if (count > 0) count else 0
        return timesCharged * amount
    }

    fun formatCurrency(amount: Double, currency: String = "USD"): String {
        val symbol = when (currency.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "JPY" -> "¥"
            "SAR" -> "SR "
            "AED" -> "AED "
            "ETB" -> "Br "
            else -> "$"
        }
        return String.format(Locale.US, "%s%,.2f", symbol, amount)
    }

    fun formatDate(dateMs: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        return sdf.format(Date(dateMs))
    }

    fun getDueWithinDays(subscriptions: List<SubscriptionEntity>, dbDays: Int): List<SubscriptionEntity> {
        return subscriptions.filter { s ->
            val d = daysUntil(s.nextRenewalDate)
            d in 0..dbDays
        }
    }
}
