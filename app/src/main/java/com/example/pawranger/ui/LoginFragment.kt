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
import com.example.pawranger.firebase.FirebaseAuthManager
import com.example.pawranger.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                    // 1. Login ke Firebase Auth
                    val result = FirebaseAuthManager.auth
                        .signInWithEmailAndPassword(emailInput, password)
                        .await()

                    val userId = result.user?.uid

                    // 2. Ambil data tambahan (nama) dari Firestore
                    var userName = emailInput.substringBefore("@")
                    if (userId != null) {
                        val doc = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .get()
                            .await()
                        val nameFromDb = doc.getString("name")
                        if (!nameFromDb.isNullOrEmpty()) {
                            userName = nameFromDb
                        }
                    }

                    // 3. Simpan ke SessionManager lokal
                    sessionManager.setLoggedIn(true)
                    sessionManager.saveEmail(emailInput)
                    sessionManager.saveUserName(userName)
                    sessionManager.saveUserId(userId ?: "")

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