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
import com.example.pawranger.data.SupabaseConfig
import com.example.pawranger.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val id: Long = 0,
    val nama: String? = null,
    val no_telp: String? = null,
    val status_sos: Boolean? = null,
    val alamat: String? = null
)

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
                    SupabaseConfig.client!!.auth.signOut()
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

        // Load profil dari Supabase
        loadProfile(view)
    }

    private fun loadProfile(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val client = SupabaseConfig.client!!

                // Ambil user yang sedang login
                val user = client.auth.currentUserOrNull()

                if (user != null) {
                    // Ambil data profil dari tabel profiles berdasarkan user id
                    val profiles = client.postgrest.from("profiles")
                        .select(Columns.ALL) {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<ProfileData>()

                    if (profiles.isNotEmpty()) {
                        val profile = profiles.first()
                        val nama = profile.nama ?: sessionManager.getUserName() ?: "User"
                        val noTelp = profile.no_telp ?: "-"
                        val alamat = profile.alamat ?: "-"   // ← tambah di sini

                        view.findViewById<TextView>(R.id.tv_profile_name_header).text = nama
                        view.findViewById<TextView>(R.id.tv_profile_name).text = nama
                        view.findViewById<TextView>(R.id.tv_profile_phone)?.text = noTelp
                        view.findViewById<TextView>(R.id.tv_profile_address)?.text = alamat  // ← tambah di sini
                    } else {
                        // Profil belum ada di Supabase, tampilkan dari session lokal
                        val userName = sessionManager.getUserName() ?: "User"
                        view.findViewById<TextView>(R.id.tv_profile_name_header).text = userName
                        view.findViewById<TextView>(R.id.tv_profile_name).text = userName
                    }
                } else {
                    // Tidak ada session, pakai data lokal
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