package com.example.pawranger.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.Chat

class ChatAdapter(
    private val chatList: List<Chat>,
    private val onItemClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSenderName: TextView = view.findViewById(R.id.tv_sender_name)
        val tvLastMessage: TextView = view.findViewById(R.id.tv_last_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.tvSenderName.text = chat.senderName
        holder.tvLastMessage.text = chat.lastMessage
        
        holder.itemView.setOnClickListener {
            onItemClick(chat)
        }
    }

    override fun getItemCount(): Int = chatList.size
}
