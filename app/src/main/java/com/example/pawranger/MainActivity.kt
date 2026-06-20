package com.example.pawranger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pawranger.data.EmergencyAlert
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.utils.PhoneUtils
import com.example.pawranger.utils.ViewUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private var mediaPlayer: MediaPlayer? = null
    private var activeDialog: AlertDialog? = null
    private var lastAlertPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        startGlobalSOSListener()

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) ViewUtils.setupSystemBarsInsets(mainView)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)

            if (bottomNavView != null) {
                bottomNavView.setupWithNavController(navController)
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    bottomNavContainer?.visibility = when (destination.id) {
                        R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }
    }

    private fun startGlobalSOSListener() {
        val db = FirebaseFirestore.getInstance()
        val rawPhone = sessionManager.getUserPhone() ?: ""
        val myPhone = PhoneUtils.formatPhoneNumber(rawPhone)

        if (myPhone.isEmpty()) return

        // Filter: Hanya dengerin alert yang statusnya ACTIVE DAN nomor gue ada di daftar kontak korban - Fase 3
        db.collection("emergency_alerts")
            .whereEqualTo("status", "ACTIVE")
            .whereArrayContains("targetContacts", myPhone)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MainActivity", "Listen failed: ${e.message}. Pastikan Index Firestore sudah dibuat.")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val latestDoc = snapshots.documents.first()
                    val alert = latestDoc.toObject(EmergencyAlert::class.java)

                    if (alert != null) {
                        handleActiveAlert(alert)
                    }
                } else {
                    // Fase 5: Auto-Mute jika status berubah jadi RESOLVED
                    stopAlarmAndDialog()
                }
            }
    }

    private fun handleActiveAlert(alert: EmergencyAlert) {
        // Ganti victimPhone pakai userId sesuai cetakan data terbaru
        val currentPhone = alert.userId ?: "unknown"

        // Fase 3: Alarm Brutal
        if (mediaPlayer == null) {
            startAlarm()
        }

        // Fase 3: Update Pop-Up jika lokasi berubah (setiap 2 menit)
        if (activeDialog != null && lastAlertPhone == currentPhone) {
            activeDialog?.setMessage("Nomor: ${alert.userId}\nUpdate Lokasi: ${alert.latitude}, ${alert.longitude}")
            activeDialog?.setTitle("⚠️ UPDATE LOKASI DARURAT!")
        } else {
            showSOSDialog(alert)
        }
        lastAlertPhone = currentPhone
    }

    private fun showSOSDialog(alert: EmergencyAlert) {
        activeDialog?.dismiss()

        activeDialog = MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ SINYAL DARURAT!")
            // Ganti victimPhone dan victimName jadi userId
            .setMessage("Nomor: ${alert.userId}\nLokasi: ${alert.latitude}, ${alert.longitude}")
            .setCancelable(false)
            .setPositiveButton("Buka Google Maps") { _, _ ->
                stopAlarm()
                openInGoogleMaps(alert.latitude ?: 0.0, alert.longitude ?: 0.0)
            }
            .setNegativeButton("Matikan Alarm") { _, _ ->
                stopAlarm()
            }
            .show()
    }

    private fun stopAlarmAndDialog() {
        stopAlarm()
        activeDialog?.dismiss()
        activeDialog = null
        lastAlertPhone = null
    }

    private fun startAlarm() {
        try {
            val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(this, alertSound)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("MainActivity", "Gagal putar alarm: ${e.message}")
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun openInGoogleMaps(lat: Double, lng: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback ke browser jika tidak ada app Google Maps
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")))
        }
    }
}