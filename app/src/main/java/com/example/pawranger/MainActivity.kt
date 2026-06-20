package com.example.pawranger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.pawranger.data.EmergencyAlert
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.utils.ViewUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val sosRepository = SOSRepository()
    private lateinit var sessionManager: SessionManager
    private var trackingJob: Job? = null
    private var lastAlert: EmergencyAlert? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate started")
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "setContentView finished")

        sessionManager = SessionManager(this)
        startSOSListener()

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewUtils.setupSystemBarsInsets(mainView)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        
        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
            val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            val fragmentContainer = findViewById<View>(R.id.nav_host_fragment)
            if (fragmentContainer != null) {
                // Padding bawah agar konten tidak tertutup bottom nav melayang
                val paddingPx = (130 * resources.displayMetrics.density).toInt()
                fragmentContainer.setPadding(0, 0, 0, paddingPx)
            }
            
            if (bottomNavView != null) {
                bottomNavView.setOnItemSelectedListener { item ->
                    val navOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .setPopUpTo(
                            navController.graph.startDestinationId,
                            inclusive = false,
                            saveState = true
                        )
                        .build()
                    
                    navController.navigate(item.itemId, null, navOptions)
                    true
                }
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    val menuItem = bottomNavView.menu.findItem(destination.id)
                    if (menuItem != null) {
                        menuItem.isChecked = true
                    }
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (bottomNavContainer != null) {
                    when (destination.id) {
                        R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> {
                            bottomNavContainer.visibility = View.GONE
                        }
                        else -> {
                            bottomNavContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            updateFcmTokenToSupabase(token)
        }
    }

    private fun updateFcmTokenToSupabase(token: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("SUPABASE_FCM", "Token FCM: $token")
            } catch (e: Exception) {
                Log.e("SUPABASE_FCM", "Error: ${e.message}")
            }
        }
    }

    private fun startSOSListener() {
        val rawPhone = sessionManager.getUserPhone() ?: ""
        val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.isEmpty()) return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                sosRepository.connect()
                sosRepository.listenForAlerts(cleanPhone).collect { alert ->
                    lastAlert = alert
                    showSOSDialog(alert)
                    startPeriodicReminders()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPeriodicReminders() {
        trackingJob?.cancel()
        trackingJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(120000)
                lastAlert?.let { alert ->
                    showSOSDialog(alert, isReminder = true)
                }
            }
        }
    }

    private fun showSOSDialog(alert: EmergencyAlert, isReminder: Boolean = false) {
        if (!isReminder) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, alertSound)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val title = if (isReminder) "⚠️ UPDATE LOKASI DARURAT!" else "⚠️ SINYAL DARURAT!"
        
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage("Nomor: ${alert.sender_phone}\nLokasi: ${alert.latitude}, ${alert.longitude}")
            .setCancelable(false)
            .setPositiveButton("Buka Google Maps") { _, _ ->
                stopAlarm()
                openInGoogleMaps(alert.latitude, alert.longitude)
            }
            .setNeutralButton("Berhenti Melacak") { _, _ ->
                stopTracking()
            }
            .setNegativeButton(if (isReminder) "Tutup" else "Matikan Alarm") { _, _ ->
                stopAlarm()
            }
            .show()
    }

    private fun openInGoogleMaps(lat: Double, lng: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
            startActivity(browserIntent)
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun stopTracking() {
        stopAlarm()
        trackingJob?.cancel()
        trackingJob = null
        lastAlert = null
    }
}
