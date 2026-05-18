package com.gramasethu.app.utils

object TimeUtils {
    fun getTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hrs ago"
            else -> "$days days ago"
        }
    }
}