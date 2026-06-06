package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.data.SupabaseConfig
import com.example.pawranger.utils.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName     = view.findViewById<EditText>(R.id.et_name)
        val etEmail    = view.findViewById<EditText>(R.id.et_register_email)
        val etPhone    = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_register_password)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val cbTerms    = view.findViewById<CheckBox>(R.id.cb_terms)

        cbTerms.text = HtmlCompat.fromHtml(
            getString(R.string.terms_agreement_html),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        btnRegister.setOnClickListener {
            val name       = etName.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val phone      = etPhone?.text.toString().trim()
            val password   = etPassword.text.toString().trim()

            if (name.isEmpty() || emailInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 1. Daftar ke Supabase Auth
                    SupabaseConfig.client!!.auth.signUpWith(Email) {
                        this.email = emailInput
                        this.password = password
                    }

                    // 2. Ambil user_id yang baru dibuat
                    val userId = SupabaseConfig.client!!.auth.currentUserOrNull()?.id

                    // 3. Update nama & no_telp ke tabel profiles
                    if (userId != null) {
                        SupabaseConfig.client!!.postgrest.from("profiles")
                            .update(mapOf("nama" to name, "no_telp" to phone)) {
                                filter { eq("user_id", userId) }
                            }
                    }

                    // 4. Simpan ke SessionManager lokal
                    sessionManager.setLoggedIn(true)
                    sessionManager.saveUserName(name)
                    sessionManager.saveEmail(emailInput)
                    sessionManager.saveUserId(userId ?: "")

                    Toast.makeText(requireContext(), "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal daftar: ${e.message}", Toast.LENGTH_LONG).show()
                    btnRegister.isEnabled = true
                }
            }
        }

        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<TextView>(R.id.tv_login_footer).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}