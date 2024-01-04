package com.example.diarycourse.features.dialogs

interface DialogListener {
    fun onConfirmAddDialogResult(title: String, text: String, date: String, timeStart: String, timeEnd: String)
//    fun onScheduleItemDeleted(isDelete: Boolean)
}
