package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Logout (ID-nya tetap sama dari Aliya)
        view.findViewById<View>(R.id.btn_logout)?.setOnClickListener {
            sessionManager.setLoggedIn(false)
            findNavController().navigate(R.id.loginFragment)
        }

        loadProfile(view)
    }

    private fun loadProfile(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Ambil nomor HP dari sesi lokal
                val rawPhone = sessionManager.getUserPhone() ?: ""
                val noTelp = rawPhone.replace(Regex("[^0-9+]"), "")

                if (noTelp.isNotEmpty()) {
                    val doc = FirebaseFirestore.getInstance()
                        .collection("profiles")
                        .document(noTelp)
                        .get()
                        .await()

                    if (doc.exists()) {
                        val nama = doc.getString("nama") ?: sessionManager.getUserName() ?: "User"

                        // Pasang data ke ID UI baru buatan Aliya
                        view.findViewById<TextView>(R.id.tv_profile_name)?.text = nama
                        view.findViewById<TextView>(R.id.tv_display_name)?.text = nama
                        view.findViewById<TextView>(R.id.tv_display_phone)?.text = rawPhone
                    } else {
                        // Kalau belum sinkron di database, pakai data lokal dulu
                        val userName = sessionManager.getUserName() ?: "User"
                        view.findViewById<TextView>(R.id.tv_profile_name)?.text = userName
                        view.findViewById<TextView>(R.id.tv_display_name)?.text = userName
                        view.findViewById<TextView>(R.id.tv_display_phone)?.text = rawPhone
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal load profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}