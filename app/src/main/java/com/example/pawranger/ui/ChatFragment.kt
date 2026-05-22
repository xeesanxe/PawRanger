package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.Chat
import com.example.pawranger.ui.adapter.ChatAdapter

class ChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatList = listOf(
            Chat("Ibu", "Lagi dimana nak? Pulang jam berapa?"),
            Chat("Ayah", "Hati-hati di jalan ya, kabari kalau sudah sampai."),
            Chat("Kakak", "Dek, titip belikan makanan pas pulang dong."),
            Chat("Adik", "Mbak, besok ada acara nggak?"),
            Chat("Paman", "Gimana kabarnya? Kapan main ke rumah?")
        )

        val rvChat = view.findViewById<RecyclerView>(R.id.rv_chat)
        rvChat.layoutManager = LinearLayoutManager(requireContext())
        rvChat.adapter = ChatAdapter(chatList) { chat ->
            val bundle = bundleOf("senderName" to chat.senderName)
            findNavController().navigate(R.id.action_navigation_chat_to_chatDetailFragment, bundle)
        }

        view.findViewById<View>(R.id.iv_profile_top)?.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }
    }
}
