package com.example.pawranger.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.SosService
import com.example.pawranger.utils.SessionManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var tvCurrentAddress: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(requireContext())
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvCurrentAddress = view.findViewById(R.id.tv_current_address)
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
                    updateAddressText(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = sessionManager.getUserName() ?: "Ranger"
        view.findViewById<TextView>(R.id.tv_greeting)?.text = "Hai, $userName"

        updateSosUI(sessionManager.isSosActive())

        view.findViewById<View>(R.id.cv_sos).setOnLongClickListener {
            toggleSosMode()
            true
        }

        view.findViewById<View>(R.id.header_home).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        view.findViewById<View>(R.id.fab_my_location)?.setOnClickListener {
            moveToCurrentLocation()
        }

        // TOMBOL LAYERS (Ganti tampilan ke Satelit/3D)
        view.findViewById<View>(R.id.fab_layers)?.setOnClickListener {
            val currentType = googleMap?.mapType
            googleMap?.mapType = when (currentType) {
                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
                GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_HYBRID
                else -> GoogleMap.MAP_TYPE_NORMAL
            }
            
            // Berikan efek tilt (miring) sedikit agar gedung-gedung terlihat 3D
            if (googleMap?.mapType != GoogleMap.MAP_TYPE_NORMAL) {
                val currentPos = googleMap?.cameraPosition
                currentPos?.let {
                    val newPos = com.google.android.gms.maps.model.CameraPosition.Builder(it)
                        .tilt(45f) // Kemiringan 45 derajat untuk efek 3D
                        .build()
                    googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(newPos))
                }
            } else {
                // Balikkan ke tampilan datar jika normal
                val currentPos = googleMap?.cameraPosition
                currentPos?.let {
                    val newPos = com.google.android.gms.maps.model.CameraPosition.Builder(it)
                        .tilt(0f)
                        .build()
                    googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(newPos))
                }
            }
        }
    }

    private fun toggleSosMode() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            Toast.makeText(context, "Izinkan akses lokasi dulu ya!", Toast.LENGTH_SHORT).show()
            return
        }

        val isNowActive = !sessionManager.isSosActive()
        sessionManager.setSosActive(isNowActive)

        if (isNowActive) {
            val intent = Intent(requireContext(), SosService::class.java).apply { action = "START" }
            ContextCompat.startForegroundService(requireContext(), intent)
            Toast.makeText(context, "🆘 SOS AKTIF!", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(requireContext(), SosService::class.java).apply { action = "STOP" }
            requireContext().startService(intent)
            Toast.makeText(context, "✅ Sinyal SOS Dimatikan.", Toast.LENGTH_SHORT).show()
        }

        updateSosUI(isNowActive)
    }

    private fun updateSosUI(isActive: Boolean) {
        val cvSos = view?.findViewById<View>(R.id.cv_sos)
        if (isActive) {
            cvSos?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#16A34A"))
        } else {
            cvSos?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DC2626"))
        }
    }

    private fun updateAddressText(lat: Double, lng: Double) {
        if (!isAdded) return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressLine = addresses[0].getAddressLine(0)
                    withContext(Dispatchers.Main) {
                        tvCurrentAddress.text = addressLine
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Geocoder error: ${e.message}")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        checkLocationPermission()
        
        // Initial move to a default place while waiting for GPS
        val jakarta = LatLng(-6.2088, 106.8456)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 10f))
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        googleMap?.isMyLocationEnabled = true

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        moveToCurrentLocation()
    }

    private fun moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                updateAddressText(it.latitude, it.longitude)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
