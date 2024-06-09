package com.easyflow.diarycourse.core.models

import com.easyflow.diarycourse.R

data class AlertData(
    val title: Int = R.string.error_title,
    val message: Int,
    val positiveButton: Int = R.string.error_understand,
    val isNegativeButtonNeeded: Boolean = false,
    val negativeButton: Int = R.string.cancel,
    val navigate: (() -> Unit)? = null
)
