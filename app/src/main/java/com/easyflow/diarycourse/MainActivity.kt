package com.easyflow.diarycourse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.easyflow.diarycourse.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        setNavigation()
    }

    private fun createNotificationChannel() {
        val channelId = "alarm_id"
        val channelName = "alarm_name"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = Color.GREEN
        notificationManager.createNotificationChannel(channel)
    }

    private fun setNavigation() {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.itemActiveIndicatorColor = getColorStateList(R.color.bgNavActiveItem)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    bottomNavigationView.menu.findItem(R.id.homeFragment)?.setIcon(R.drawable.ic_menu_home_selected)
                    bottomNavigationView.menu.findItem(R.id.calendarFragment)?.setIcon(R.drawable.ic_menu_calendar)
                    bottomNavigationView.menu.findItem(R.id.settingsFragment)?.setIcon(R.drawable.ic_menu_settings)
                }
                R.id.calendarFragment -> {
                    bottomNavigationView.menu.findItem(R.id.homeFragment)?.setIcon(R.drawable.ic_menu_home)
                    bottomNavigationView.menu.findItem(R.id.calendarFragment)?.setIcon(R.drawable.ic_menu_calendar_selected)
                    bottomNavigationView.menu.findItem(R.id.settingsFragment)?.setIcon(R.drawable.ic_menu_settings)
                }
                R.id.settingsFragment -> {
                    bottomNavigationView.menu.findItem(R.id.homeFragment)?.setIcon(R.drawable.ic_menu_home)
                    bottomNavigationView.menu.findItem(R.id.calendarFragment)?.setIcon(R.drawable.ic_menu_calendar)
                    bottomNavigationView.menu.findItem(R.id.settingsFragment)?.setIcon(R.drawable.ic_menu_settings_selected)
                }
            }
        }
    }

}