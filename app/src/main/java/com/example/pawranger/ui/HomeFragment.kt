package com.example.pawranger.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions


class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
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

        // Add some basic interaction for FABs
        view.findViewById<View>(R.id.fab_my_location)?.setOnClickListener {
            val sudirman = LatLng(-6.2248, 106.8073)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(sudirman, 17f))
        }

        view.findViewById<View>(R.id.fab_layers)?.setOnClickListener {
            val nextType = when (googleMap?.mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
                else -> GoogleMap.MAP_TYPE_NORMAL
            }
            googleMap?.mapType = nextType
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        try {
            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
        } catch (_: Exception) {
        }

        val defaultLocation = LatLng(-6.2248, 106.8073)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16f))
        
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        
        googleMap?.addMarker(MarkerOptions().position(defaultLocation).title("Lokasi Saya"))
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
