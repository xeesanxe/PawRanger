package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set data user dari session
        val userName = sessionManager.getUserName() ?: "Budi Santoso"
        view.findViewById<TextView>(R.id.tv_profile_name_header).text = userName
        view.findViewById<TextView>(R.id.tv_profile_name).text = userName

        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Logout
        view.findViewById<MaterialCardView>(R.id.btn_logout).setOnClickListener {
            sessionManager.setLoggedIn(false)
            findNavController().navigate(R.id.loginFragment)
        }
        
        // Setup klik untuk edit (bisa ditambahkan dialog nanti)
        view.findViewById<View>(R.id.iv_edit_name).setOnClickListener { 
            // Aksi edit nama
        }
    }
}