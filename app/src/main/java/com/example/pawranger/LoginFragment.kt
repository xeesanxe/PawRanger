package com.example.pawranger

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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

class LoginFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val sosRepository = SOSRepository()
    private var isPasswordVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_password)

        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            val rawPhone = etPhone.text.toString().trim()
            val password = etPassword?.text?.toString()?.trim() ?: ""

            if (rawPhone.isNotEmpty()) {
                val cleanPhone = formatPhoneNumber(rawPhone)

                // Ambil Token terbaru
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(context, "Gagal memuat layanan notifikasi", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val newToken = task.result

                    // Sinkronisasi ke Supabase
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val userName = sosRepository.loginUserAndSyncToken(cleanPhone, newToken)

                            withContext(Dispatchers.Main) {
                                if (userName != null) {
                                    sessionManager.setLoggedIn(true)
                                    sessionManager.saveUserPhone(cleanPhone)
                                    sessionManager.saveUserName(userName) // Update nama dari database

                                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                                } else {
                                    Toast.makeText(context, "Nomor belum terdaftar, silakan Register", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val ivShowPassword = view.findViewById<ImageView>(R.id.iv_show_password)

        ivShowPassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        view.findViewById<MaterialButton>(R.id.btn_login).setOnClickListener {
            val phone = etPhone.text.toString()
            if (phone.isNotEmpty()) {
                sessionManager.setLoggedIn(true)
                sessionManager.saveUserPhone(phone)
                findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
            } else {
                etPhone.error = "Nomor Telepon harus diisi"
            }
        }

        view.findViewById<TextView>(R.id.tv_register_footer).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}

    // Fungsi penyaring nomor
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