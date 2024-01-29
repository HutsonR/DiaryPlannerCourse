package com.easyflow.diarycourse.domain.util

sealed class Resource {
    object Success : Resource()
    sealed class Empty : Resource() {
        object Failed : Empty()
    }
}