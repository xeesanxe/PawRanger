package com.example.pawranger.ui

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.R
import com.example.pawranger.data.MessageRepository
import com.example.pawranger.utils.SessionManager
import kotlinx.coroutines.launch

class ChatDetailFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val name = arguments?.getString("senderName") ?: "Chat"
        val receiverId = arguments?.getString("receiverId") ?: ""
        val currentUserId = sessionManager.getUserId() ?: ""

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

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isEmpty()) return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch {
                val berhasil = MessageRepository.sendMessage(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content
                )
                if (berhasil) {
                    etMessage.text.clear()
                    Toast.makeText(context, "Pesan terkirim!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnVoice.setOnLongClickListener {
            Toast.makeText(context, "Merekam suara...", Toast.LENGTH_SHORT).show()
            true
        }
    }
}