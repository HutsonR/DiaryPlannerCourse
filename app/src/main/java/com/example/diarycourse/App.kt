package com.example.diarycourse

import android.app.Application
import com.example.diarycourse.DaggerAppComponent

class App: Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(application = this)
            .build()
    }
}