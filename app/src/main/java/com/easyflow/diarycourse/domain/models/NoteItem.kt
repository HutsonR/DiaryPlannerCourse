package com.easyflow.diarycourse.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteItem(
    val id: Int? = null,
    val text: String,
    var date: String
): Parcelable