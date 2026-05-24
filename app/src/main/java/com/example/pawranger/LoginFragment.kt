package com.example.pawranger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager

class LoginFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        
        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                sessionManager.setLoggedIn(true)
                sessionManager.saveUserEmail(email)
                findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
            } else {
                etEmail.error = "Email harus diisi"
            }
        }

        view.findViewById<TextView>(R.id.tv_register_footer).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
