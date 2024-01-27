package com.example.diarycourse.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteItem(
    val id: Int? = null,
    val text: String,
    val date: String
): Parcelable