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
                        R.id.splashFragment,
                        R.id.loginFragment,
                        R.id.registerFragment,
                        R.id.addContactFragment -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }

        // 🔥 NANGKEP TENDANGAN FCM PAS APLIKASI MATI TOTAL
        val isEmergency = intent.getBooleanExtra("isEmergency", false)
        if (isEmergency) {
            val senderName = intent.getStringExtra("senderName") ?: "Seseorang"
            val senderPhone = intent.getStringExtra("senderPhone") ?: "Tidak diketahui"
            val lat = intent.getStringExtra("lat")?.toDoubleOrNull() ?: 0.0
            val lng = intent.getStringExtra("lng")?.toDoubleOrNull() ?: 0.0

            // Bikin objek palsu buat mancing fungsi handleActiveAlert
            val alertData = EmergencyAlert(
                latitude = lat,
                longitude = lng,
                userId = senderPhone,
                senderName = senderName
            )

            // Langsung lempar ke mesin pencari kontak cerdas kita
            handleActiveAlert(alertData)
        }
    }

    private fun startGlobalSOSListener() {
        val db = FirebaseFirestore.getInstance()
        val rawPhone = sessionManager.getUserPhone() ?: ""
        val myPhone = PhoneUtils.formatPhoneNumber(rawPhone)

        if (myPhone.isEmpty()) return

        db.collection("emergency_alerts")
            .whereEqualTo("status", "ACTIVE")
            .whereArrayContains("targetContacts", myPhone)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MainActivity", "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val latestDoc = snapshots.documents.first()
                    val alert = latestDoc.toObject(EmergencyAlert::class.java)

                    if (alert != null) {
                        handleActiveAlert(alert)
                    }
                } else {
                    stopAlarmAndDialog()
                }
            }
    }

    private fun handleActiveAlert(alert: EmergencyAlert) {
        val senderPhone = alert.userId ?: "Tidak diketahui"
        val fallbackName = alert.senderName ?: "Seseorang"

        if (senderPhone == "Tidak diketahui") return

        val db = FirebaseFirestore.getInstance()
        val myPhone = PhoneUtils.formatPhoneNumber(sessionManager.getUserPhone() ?: "")

        // CERDAS: Cari nama pengirim di database kontak lokal penyelamat
        db.collection("contacts")
            .whereEqualTo("userId", myPhone)
            .whereEqualTo("phoneNumber", senderPhone)
            .get()
            .addOnSuccessListener { snapshot ->
                val finalName = if (!snapshot.isEmpty) {
                    snapshot.documents.first().getString("name") ?: fallbackName
                } else {
                    fallbackName
                }
                displayAlertUI(alert, finalName, senderPhone)
            }
            .addOnFailureListener {
                displayAlertUI(alert, fallbackName, senderPhone)
            }
    }

    private fun displayAlertUI(alert: EmergencyAlert, senderName: String, senderPhone: String) {
        if (mediaPlayer == null) {
            startAlarm()
        }

        if (activeDialog != null && lastAlertPhone == senderPhone) {
            activeDialog?.setMessage("Dari: $senderName\nNomor: $senderPhone\nUpdate Lokasi: ${alert.latitude}, ${alert.longitude}")
            activeDialog?.setTitle("⚠️ UPDATE LOKASI DARURAT!")
        } else {
            showSOSDialog(alert, senderName, senderPhone)
        }
        lastAlertPhone = senderPhone
    }

    private fun showSOSDialog(alert: EmergencyAlert, name: String, phone: String) {
        activeDialog?.dismiss()

        activeDialog = MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ SINYAL DARURAT!")
            .setMessage("Dari: $name\nNomor: $phone\nLokasi: ${alert.latitude}, ${alert.longitude}")
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
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")))
        }
    }
}