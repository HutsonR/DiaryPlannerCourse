package com.example.diarycourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.diarycourse.features.feature_home.HomeFragment
import com.example.diarycourse.features.feature_schedule.ScheduleFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()
        }
    }
}