package com.example.diarycourse.features.feature_home.schedule.dialogs

import com.example.diarycourse.features.feature_home.schedule.utils.Color
import com.example.diarycourse.features.feature_home.schedule.utils.Priority

interface DialogListener {
    fun onConfirmAddDialogResult(title: String, text: String, date: String, priority: Priority, timeStart: String, timeEnd: String, color: Color)
}
