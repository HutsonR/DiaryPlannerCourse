package com.easyflow.diarycourse

import android.app.Application

class App: Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(application = this)
            .build()
    }
}