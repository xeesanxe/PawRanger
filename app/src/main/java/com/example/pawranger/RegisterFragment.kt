package com.example.pawranger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val sosRepository = SOSRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Hubungkan ID dari XML Aliya ke Kotlin
        val etName = view.findViewById<EditText>(R.id.et_name)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_register_password) // Ini yang bikin merah tadi
        val cbTerms = view.findViewById<CheckBox>(R.id.cb_terms)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btn_register)

        btnRegister.setOnClickListener {
            // 2. Ambil teks yang diketik user & hilangkan spasi typo (.trim)
            val name = etName.text.toString().trim()
            val rawPhone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 3. Cek apakah kolom kosong
            if (name.isNotEmpty() && rawPhone.isNotEmpty() && password.isNotEmpty()) {

                // Cek centang persetujuan
                if (!cbTerms.isChecked) {
                    Toast.makeText(context, "Kamu harus menyetujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Format nomor HP biar seragam jadi awalan 08...
                val cleanPhone = formatPhoneNumber(rawPhone)

                // Simpan di memori lokal (HP)
                sessionManager.saveUserName(name)
                sessionManager.saveUserPhone(cleanPhone)
                sessionManager.setLoggedIn(true)

                // Ambil Token Firebase & Kirim ke Server database Josua
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM", "Gagal ambil token Firebase", task.exception)
                        return@addOnCompleteListener
                    }

                    val fcmToken = task.result

                    // Jalankan fungsi kirim ke internet di background
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sosRepository.saveUserProfile(name, cleanPhone, fcmToken)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Registrasi & Sinkronisasi Berhasil!", Toast.LENGTH_SHORT).show()
                                // Lanjut ke halaman Login setelah sukses
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Gagal simpan ke server: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Harap lengkapi Nama, Nomor Telepon, dan Kata Sandi", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol buat pindah ke halaman Login kalau udah punya akun
        view.findViewById<TextView>(R.id.tv_login_footer).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    // Fungsi penyaring nomor (sudah aman di luar onViewCreated)
    private fun formatPhoneNumber(phone: String?): String {
        if (phone.isNullOrEmpty()) return ""
        val numOnly = phone.replace(Regex("[^0-9+]"), "")
        return when {
            numOnly.startsWith("+62") -> "0" + numOnly.substring(3)
            numOnly.startsWith("62") -> "0" + numOnly.substring(2)
            else -> numOnly
        }
    }
}