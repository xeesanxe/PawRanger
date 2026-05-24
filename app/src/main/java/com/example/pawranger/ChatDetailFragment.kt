package com.example.pawranger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ChatDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("senderName") ?: "Shane Martinez"
        view.findViewById<TextView>(R.id.tv_detail_name).text = name

        view.findViewById<ImageButton>(R.id.btn_back_chat).setOnClickListener {
            findNavController().navigateUp()
        }

        val etMessage = view.findViewById<EditText>(R.id.et_message)
        val btnVoice = view.findViewById<ImageButton>(R.id.btn_voice_note)
        val btnSend = view.findViewById<ImageButton>(R.id.btn_send_message)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    btnVoice.visibility = View.VISIBLE
                    btnSend.visibility = View.GONE
                } else {
                    btnVoice.visibility = View.GONE
                    btnSend.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnVoice.setOnClickListener {
            Toast.makeText(context, "Tekan lama untuk merekam pesan suara", Toast.LENGTH_SHORT).show()
        }

        btnVoice.setOnLongClickListener {
            Toast.makeText(context, "Merekam suara...", Toast.LENGTH_SHORT).show()
            true
        }

        btnSend.setOnClickListener {
            Toast.makeText(context, "Pesan terkirim!", Toast.LENGTH_SHORT).show()
            etMessage.text.clear()
        }
    }
}
