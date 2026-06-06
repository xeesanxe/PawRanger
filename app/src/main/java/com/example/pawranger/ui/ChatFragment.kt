package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.Chat
import com.example.pawranger.data.MessageRepository
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.ui.adapter.ChatAdapter
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<Chat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val rvChat = view.findViewById<RecyclerView>(R.id.rv_chat)
        rvChat.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter(chatList) { chat ->
            val bundle = bundleOf(
                "senderName" to chat.senderName,
                "receiverId" to chat.receiverId,
                "senderId" to chat.senderId
            )
            findNavController().navigate(
                R.id.action_navigation_chat_to_chatDetailFragment, bundle
            )
        }
        rvChat.adapter = chatAdapter

        loadConversations()
    }

    private fun loadConversations() {
        val currentUserId = sessionManager.getUserId() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val messages = MessageRepository.getConversations(currentUserId)

            // Kelompokkan berdasarkan lawan bicara
            val seen = mutableSetOf<String>()
            val conversations = mutableListOf<Chat>()

            for (msg in messages) {
                val otherUserId = if (msg.senderId == currentUserId) msg.receiverId else msg.senderId
                if (seen.add(otherUserId)) {
                    conversations.add(
                        msg.copy(
                            senderName = otherUserId, // Nanti bisa diganti nama dari contacts
                            lastMessage = msg.content
                        )
                    )
                }
            }

            chatList.clear()
            chatList.addAll(conversations)
            chatAdapter.notifyDataSetChanged()
        }
    }
}