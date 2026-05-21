package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.CallLog
import com.example.pawranger.ui.adapter.CallLogAdapter

class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callLogs = listOf(
            CallLog("Ayah", "Panggilan Masuk - 10:30"),
            CallLog("Mamah", "Panggilan Tidak Terjawab - 09:15"),
            CallLog("081234567890", "Panggilan Keluar - Kemarin")
        )

        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_call_history)
        rvHistory.adapter = CallLogAdapter(callLogs)

        view.findViewById<View>(R.id.iv_profile_top)?.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }
    }
}