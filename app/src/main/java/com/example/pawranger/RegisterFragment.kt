package com.example.pawranger

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val sosRepository = SOSRepository()
    private var isPasswordVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.et_name)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etPassword = view.findViewById<EditText>(R.id.et_register_password)
        val cbTerms = view.findViewById<CheckBox>(R.id.cb_terms)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btn_register)
        val ivShowPassword = view.findViewById<ImageView>(R.id.iv_show_password_register)
        val tvLoginFooter = view.findViewById<TextView>(R.id.tv_login_footer)

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

        tvLoginFooter?.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val rawPhone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || rawPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Harap lengkapi Nama, Nomor Telepon, dan Kata Sandi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(context, "Kamu harus menyetujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            val cleanPhone = PhoneUtils.formatPhoneNumber(rawPhone)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    sosRepository.saveUserProfile(name, cleanPhone, fcmToken)

                    sessionManager.saveUserName(name)
                    sessionManager.saveUserPhone(cleanPhone)
                    sessionManager.setLoggedIn(true)

                    Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_navigation_home)
                } catch (e: Exception) {
                    Log.e("Register", "Gagal register: ${e.message}")
                    Toast.makeText(context, "Gagal simpan ke server: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnRegister.isEnabled = true
                }
            }
        }
    }
}
