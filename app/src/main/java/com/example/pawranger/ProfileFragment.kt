package com.example.pawranger

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvDisplayPhone: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.iv_profile_picture)
        tvName = view.findViewById(R.id.tv_profile_name)
        tvDisplayName = view.findViewById(R.id.tv_display_name)
        tvDisplayPhone = view.findViewById(R.id.tv_display_phone)

        // Sync Data
        val userName = sessionManager.getUserName() ?: "Aliya"
        val userPhone = sessionManager.getUserPhone() ?: "+62 812-3456-7890"

        tvName.text = userName
        tvDisplayName.text = userName
        tvDisplayPhone.text = userPhone

        sessionManager.getProfileImage()?.let {
            ivProfile.setImageURI(Uri.parse(it))
        }

        view.findViewById<View>(R.id.item_account)?.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_editAccountFragment)
        }

        view.findViewById<View>(R.id.btn_logout).setOnClickListener {
            sessionManager.logout()
            findNavController().navigate(R.id.splashFragment)
        }
    }
}
