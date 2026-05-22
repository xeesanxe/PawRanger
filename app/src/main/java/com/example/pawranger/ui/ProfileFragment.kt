package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.utils.SessionManager
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set data user dari session
        val fullName = sessionManager.getFullName() ?: "John Doe"
        val userEmail = sessionManager.getEmail() ?: "refero.john.doe@gmail.com"
        val userPhone = sessionManager.getPhone() ?: "0812-3456-7890"
        
        view.findViewById<TextView>(R.id.tv_profile_name_header).text = fullName
        view.findViewById<TextView>(R.id.tv_profile_full_name).text = fullName
        view.findViewById<TextView>(R.id.tv_profile_email).text = userEmail
        view.findViewById<TextView>(R.id.tv_profile_phone).text = userPhone

        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Logout
        view.findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        // Edit Action
        view.findViewById<View>(R.id.tv_edit_action).setOnClickListener { 
            // Aksi edit profil
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { _, _ ->
                sessionManager.logout()
                findNavController().navigate(R.id.loginFragment)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}