package com.example.pawranger

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng

import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.pawranger.data.EmergencyAlert
import com.example.pawranger.data.SOSRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private lateinit var locationCallback: LocationCallback
    private var tvCurrentAddress: TextView? = null
    private val sosRepository = SOSRepository()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        sessionManager = SessionManager(requireContext())
        
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupLocationCallback()
        
        return view
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Update TextView Alamat
                    getAddress(location.latitude, location.longitude)
                    // (Opsional) Gerakkan kamera mengikuti user jika baru pertama kali atau sedang mode tracking
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHalo = view.findViewById<TextView>(R.id.tv_halo)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_top)

        tvCurrentAddress = view.findViewById(R.id.tv_current_address)
        tvHalo.text = "Halo, ${sessionManager.getUserName()}"

        sessionManager.getProfileImage()?.let { uriString ->
            ivProfile.setImageURI(Uri.parse(uriString))
        }

        view.findViewById<View>(R.id.cv_sos).setOnLongClickListener {
            sendEmergencySOS()
            true
        }

        view.findViewById<View>(R.id.iv_profile_top).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        view.findViewById<View>(R.id.fab_my_location).setOnClickListener {
            checkLocationPermission()
        }

        view.findViewById<View>(R.id.fab_layers).setOnClickListener {
            googleMap?.let {
                it.mapType = if (it.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    GoogleMap.MAP_TYPE_HYBRID
                } else {
                    GoogleMap.MAP_TYPE_NORMAL
                }
            }
        }

        startPulseAnimation(view.findViewById(R.id.sos_pulse_1), 1.4f)
        startPulseAnimation(view.findViewById(R.id.sos_pulse_2), 1.2f)
    }

    private fun startPulseAnimation(view: View?, scale: Float) {
        if (view == null) return
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, scale)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, scale)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f)

        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
    }

    private fun sendEmergencySOS() {
        val rawPhone = sessionManager.getUserPhone() ?: ""
        // Hanya ambil angka saja
        val myPhone = rawPhone.replace(Regex("[^0-9]"), "")
        
        Toast.makeText(context, "ID Saya: $myPhone", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // 1. Ambil kontak darurat
                val contacts = sosRepository.getEmergencyContacts(myPhone)
                
                if (contacts.isEmpty()) {
                    Toast.makeText(context, "Anda belum memiliki kontak darurat!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Ambil lokasi terakhir
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            lifecycleScope.launch {
                                var successCount = 0
                                contacts.forEach { contact ->
                                    val alert = EmergencyAlert(
                                        sender_phone = myPhone,
                                        receiver_phone = contact.phoneNumber,
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                    )
                                    try {
                                        sosRepository.sendSOS(alert)
                                        successCount++
                                    } catch (e: Exception) {
                                        Log.e("SOS_ERROR", "Gagal kirim ke ${contact.phoneNumber}: ${e.message}")
                                    }
                                }
                                Toast.makeText(context, "Berhasil! SOS Terkirim ke $successCount kontak.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "GPS belum mengunci lokasi. Coba buka Google Maps dulu sebentar.", Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Gagal akses GPS: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val defaultLocation = LatLng(-6.2088, 106.8456)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getAddress(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                tvCurrentAddress?.text = address
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false // Sembunyikan button default Google
                
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMinUpdateIntervalMillis(2000)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                        getAddress(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .build()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
