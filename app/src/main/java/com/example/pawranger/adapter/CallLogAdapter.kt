package com.example.pawranger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.CallLog

class CallLogAdapter(private val callLogs: List<CallLog>) : RecyclerView.Adapter<CallLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_call_name)
        val tvDetail: TextView = view.findViewById(R.id.tv_call_detail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = callLogs[position]
        holder.tvName.text = log.name
        holder.tvDetail.text = log.detail
    }

    override fun getItemCount() = callLogs.size
}