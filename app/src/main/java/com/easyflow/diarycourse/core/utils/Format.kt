package com.easyflow.diarycourse.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatDate(day: Calendar): String {
    val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    return day.time.let { dateFormat.format(it) }
}