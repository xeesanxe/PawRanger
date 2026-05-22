package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.utils.SessionManager

class RegisterFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = view.findViewById<EditText>(R.id.et_username)
        val etFullName = view.findViewById<EditText>(R.id.et_name)
        val etEmail = view.findViewById<EditText>(R.id.et_register_email)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_register_password)
        val etConfirmPassword = view.findViewById<EditText>(R.id.et_confirm_password)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val cbTerms = view.findViewById<CheckBox>(R.id.cb_terms)

        cbTerms.text = HtmlCompat.fromHtml(
            getString(R.string.terms_agreement_html),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val fullName = etFullName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isNotEmpty() && fullName.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                if (password != confirmPassword) {
                    Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (!cbTerms.isChecked) {
                    Toast.makeText(requireContext(), "Anda harus menyetujui S&K", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Simpan data ke session
                sessionManager.saveUserName(username)
                sessionManager.saveFullName(fullName)
                sessionManager.saveEmail(email)
                sessionManager.savePhone(phone)

                Toast.makeText(requireContext(), "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Mohon isi semua data", Toast.LENGTH_SHORT).show()
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