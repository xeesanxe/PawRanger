package com.example.pawranger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private lateinit var locationCallback: LocationCallback
    private lateinit var tvCurrentAddress: TextView
    private lateinit var tvGreeting: TextView
    private var isSosActive = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocation()
        }
    }

    private fun checkLocationPermission() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        sessionManager = SessionManager(requireContext())

        tvCurrentAddress = view.findViewById(R.id.tv_current_address)
        tvGreeting = view.findViewById(R.id.tv_greeting)
        
        val userName = sessionManager.getUserName() ?: "Aliya"
        tvGreeting.text = "Hai, $userName"

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
                val location = locationResult.lastLocation ?: return
                updateAddressText(location.latitude, location.longitude)
            }
        }
    }

    private fun updateAddressText(lat: Double, lng: Double) {
        if (!isAdded) return
        
        lifecycleScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            try {
                // Menggunakan getFromLocation yang async (untuk API 33+) atau yang lama
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        tvCurrentAddress.text = address
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Geocoder error: ${e.message}")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cv_sos).setOnLongClickListener {
            toggleSosMode()
            true
        }

        view.findViewById<FloatingActionButton>(R.id.fab_my_location)?.setOnClickListener {
            enableMyLocation()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_layers)?.setOnClickListener {
            val nextType = when (googleMap?.mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
                else -> GoogleMap.MAP_TYPE_NORMAL
            }
            googleMap?.mapType = nextType
        }
    }

    private fun toggleSosMode() {
        if (isSosActive) {
            isSosActive = false
            val intent = Intent(requireContext(), SosService::class.java).apply { action = "STOP" }
            requireContext().stopService(intent)
            Toast.makeText(context, "Sinyal SOS Dimatikan.", Toast.LENGTH_SHORT).show()
        } else {
            isSosActive = true
            val intent = Intent(requireContext(), SosService::class.java).apply { action = "START" }
            ContextCompat.startForegroundService(requireContext(), intent)
            Toast.makeText(context, "SOS AKTIF!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("HomeFragment", "Peta berhasil dimuat (Map Ready)")
        googleMap = map
        val defaultLocation = LatLng(-6.2088, 106.8456)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        
        // Menambahkan sample marker "Safe Zones"
        addSampleMarkers()
        
        checkLocationPermission()
    }

    private fun addSampleMarkers() {
        val safeZones = listOf(
            LatLng(-6.2100, 106.8400) to "Safe Zone 1",
            LatLng(-6.2050, 106.8500) to "Safe Zone 2",
            LatLng(-6.2150, 106.8480) to "Safe Zone 3"
        )

        safeZones.forEach { (latLng, title) ->
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
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
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false

                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                        updateAddressText(it.latitude, it.longitude)
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
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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
