package com.easyflow.diarycourse.core.models

data class AlertData(
    val title: String = "Ошибка",
    val message: Int,
    val positiveButton: String = "Понятно",
    val navigate: Any? = null
)
