package com.example.pawranger.ui

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val sosRepository = SOSRepository()
    private var isPasswordVisible = false // Buat nyimpen status mata

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        val btnRegister = view.findViewById<TextView>(R.id.tv_register_footer)
        val ivShowPassword = view.findViewById<ImageView>(R.id.iv_show_password)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)

        // 1. LOGIKA IKON MATA PASSWORD
        ivShowPassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Tampilkan password (melek)
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye) // Ganti icon kalau ada (misal ic_eye_crossed)
                ivShowPassword.alpha = 0.5f // Bikin agak redup dikit tanda lagi melek
            } else {
                // Sembunyikan password (merem)
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye)
                ivShowPassword.alpha = 1.0f
            }
            // Biar kursornya tetap di akhir teks setelah diganti tipe inputnya
            etPassword.setSelection(etPassword.text.length)
        }

        // 2. TOMBOL LUPA PASSWORD
        tvForgotPassword?.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur pemulihan sandi sedang dalam pengembangan.", Toast.LENGTH_SHORT).show()
        }

        // 3. PROSES LOGIN UTAMA
        btnLogin.setOnClickListener {
            val rawPhone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (rawPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Nomor HP dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            val cleanPhone = formatPhoneNumber(rawPhone)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    val userName = sosRepository.loginUserAndSyncToken(cleanPhone, fcmToken)

                    if (userName != null) {
                        sessionManager.setLoggedIn(true)
                        sessionManager.saveUserPhone(cleanPhone)
                        sessionManager.saveUserName(userName)

                        Toast.makeText(requireContext(), "Selamat datang, $userName!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                    } else {
                        Toast.makeText(requireContext(), "Nomor HP belum terdaftar!", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Login gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                }
            }
        }

        // 4. TOMBOL PINDAH KE REGISTER
        btnRegister?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

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