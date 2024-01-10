package com.example.diarycourse.features.ui.dialogs

import com.example.diarycourse.features.ui.utils.Color

interface DialogListener {
    fun onConfirmAddDialogResult(title: String, text: String, date: String, timeStart: String, timeEnd: String, color: Color)
//    fun onScheduleItemDeleted(isDelete: Boolean)
}
