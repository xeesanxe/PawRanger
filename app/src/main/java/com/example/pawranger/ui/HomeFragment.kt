package com.example.pawranger.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.data.EmergencyAlertRepository
import com.example.pawranger.utils.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inisialisasi SessionManager buat narik data
        sessionManager = SessionManager(requireContext())
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // INI DIA OBATNYA BIAR NAMANYA NGGAK ALIYA TERUS
        val userName = sessionManager.getUserName() ?: "Ranger"
        view.findViewById<TextView>(R.id.tv_greeting)?.text = "Hai, $userName"

        val alertRepository = EmergencyAlertRepository()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        view.findViewById<View>(R.id.cv_sos).setOnLongClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Izin lokasi belum diberikan", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(context, "Lokasi tidak ditemukan, coba lagi", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val berhasil = alertRepository.sendAlert(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    if (berhasil) {
                        Toast.makeText(context, "🆘 SOS TERKIRIM!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Gagal kirim SOS, coba lagi", Toast.LENGTH_LONG).show()
                    }
                }
            }
            true
        }

        // Area header diklik buat masuk ke Profil
        view.findViewById<View>(R.id.header_home).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

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