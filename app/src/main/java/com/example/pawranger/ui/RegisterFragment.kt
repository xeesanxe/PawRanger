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

class RegisterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<TextView>(R.id.tv_login_footer).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<android.widget.Button>(R.id.btn_register).setOnClickListener {
            // Logic for registration would go here
            findNavController().navigate(R.id.action_registerFragment_to_navigation_home)
        }
    }
}
