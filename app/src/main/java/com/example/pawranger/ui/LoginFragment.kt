package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.data.SupabaseConfig
import com.example.pawranger.utils.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val emailInput = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (emailInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Login ke Supabase Auth
                    SupabaseConfig.client!!.auth.signInWith(Email) {
                        this.email = emailInput
                        this.password = password
                    }

                    // Ambil data user dari Supabase
                    val user = SupabaseConfig.client!!.auth.currentUserOrNull()
                    val userName = emailInput.substringBefore("@")

                    // Simpan ke SessionManager lokal
                    sessionManager.setLoggedIn(true)
                    sessionManager.saveEmail(emailInput)
                    sessionManager.saveUserName(userName)
                    sessionManager.saveUserId(user?.id ?: "")

                    findNavController().navigate(R.id.action_loginFragment_to_navigation_home)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Login gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                }
            }
        }

        view.findViewById<TextView>(R.id.tv_register_footer).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}