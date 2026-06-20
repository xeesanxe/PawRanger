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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.utils.PhoneUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val ivShowPassword = view.findViewById<ImageView>(R.id.iv_show_password)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btn_login)
        val tvRegisterFooter = view.findViewById<TextView>(R.id.tv_register_footer)

        ivShowPassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.alpha = 0.5f
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.alpha = 1.0f
            }
            etPassword.setSelection(etPassword.text.length)
        }

        tvRegisterFooter?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        btnLogin.setOnClickListener {
            val rawPhone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (rawPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Nomor HP dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            val cleanPhone = PhoneUtils.formatPhoneNumber(rawPhone)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val newToken = FirebaseMessaging.getInstance().token.await()
                    val userName = sosRepository.loginUserAndSyncToken(cleanPhone, newToken)

                    if (userName != null) {
                        sessionManager.setLoggedIn(true)
                        sessionManager.saveUserPhone(cleanPhone)
                        sessionManager.saveUserName(userName)

                        Toast.makeText(context, "Login Berhasil! Selamat datang $userName", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                    } else {
                        Toast.makeText(context, "Nomor belum terdaftar!", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                }
            }
        }
    }
}
