package com.example.pawranger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.et_name)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val cbTerms = view.findViewById<CheckBox>(R.id.cb_terms)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btn_register)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()

            if (!cbTerms.isChecked) {
                Toast.makeText(context, "Anda harus menyetujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                sessionManager.saveUserName(name)
                sessionManager.saveUserPhone(phone)
                Toast.makeText(context, "Registrasi Berhasil! Silakan masuk.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                Toast.makeText(context, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<TextView>(R.id.tv_login_footer).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
