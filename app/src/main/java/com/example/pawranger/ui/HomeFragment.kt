package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.iv_profile).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment)
        }

        view.findViewById<android.widget.Button>(R.id.btn_sos).setOnClickListener {
            android.widget.Toast.makeText(context, "SOS Sent!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
