package com.example.diarycourse.features.feature_home.schedule.dialogs

import com.example.diarycourse.features.feature_home.schedule.utils.Color

interface DialogListener {
    fun onConfirmAddDialogResult(title: String, text: String, date: String, timeStart: String, timeEnd: String, color: Color)
}
