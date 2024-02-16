package com.easyflow.diarycourse.core

import android.app.Application

class App: Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(application = this)
            .build()
    }
}