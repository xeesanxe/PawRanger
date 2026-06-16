package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
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

        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<MaterialCardView>(R.id.btn_logout).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    FirebaseAuth.getInstance().signOut()
                } catch (e: Exception) {
                    // ignore
                }
                sessionManager.setLoggedIn(false)
                findNavController().navigate(R.id.loginFragment)
            }
        }

        view.findViewById<View>(R.id.iv_edit_name).setOnClickListener {
            // Aksi edit nama (bisa ditambahkan dialog nanti)
        }

        loadProfile(view)
    }

    private fun loadProfile(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    val doc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (doc.exists()) {
                        val nama = doc.getString("name") ?: sessionManager.getUserName() ?: "User"
                        val noTelp = doc.getString("phone") ?: "-"
                        val alamat = doc.getString("alamat") ?: "-"

                        view.findViewById<TextView>(R.id.tv_profile_name_header).text = nama
                        view.findViewById<TextView>(R.id.tv_profile_name).text = nama
                        view.findViewById<TextView>(R.id.tv_profile_phone)?.text = noTelp
                        view.findViewById<TextView>(R.id.tv_profile_address)?.text = alamat
                    } else {
                        // Dokumen belum ada, pakai data lokal
                        val userName = sessionManager.getUserName() ?: "User"
                        view.findViewById<TextView>(R.id.tv_profile_name_header).text = userName
                        view.findViewById<TextView>(R.id.tv_profile_name).text = userName
                    }
                } else {
                    // Tidak ada user login, pakai data lokal
                    val userName = sessionManager.getUserName() ?: "User"
                    view.findViewById<TextView>(R.id.tv_profile_name_header).text = userName
                    view.findViewById<TextView>(R.id.tv_profile_name).text = userName
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal load profil: ${e.message}", Toast.LENGTH_SHORT).show()
                val userName = sessionManager.getUserName() ?: "User"
                view.findViewById<TextView>(R.id.tv_profile_name_header).text = userName
                view.findViewById<TextView>(R.id.tv_profile_name).text = userName
            }
        }
    }
}