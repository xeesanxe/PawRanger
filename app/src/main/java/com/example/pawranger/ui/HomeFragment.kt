package com.example.pawranger.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cv_sos).setOnLongClickListener {
            Toast.makeText(context, "SOS DARURAT DIKIRIM!", Toast.LENGTH_LONG).show()
            true
        }

        view.findViewById<View>(R.id.iv_profile_top).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        startPulseAnimation(view.findViewById(R.id.v_glow_1), 1.4f)
        startPulseAnimation(view.findViewById(R.id.v_glow_2), 1.2f)
    }

    private fun startPulseAnimation(view: View, scale: Float) {
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Lokasi default (Jakarta)
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

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
            
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
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
