package com.example.pawranger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.pawranger.data.Contact
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.adapter.ContactAdapter
import com.google.android.material.textfield.TextInputEditText

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pawranger.data.ContactRepository

import com.example.pawranger.utils.SessionManager

class KontakFragment : Fragment() {
    private lateinit var repository: ContactRepository
    private lateinit var adapter: ContactAdapter
    private lateinit var sessionManager: SessionManager
    private var contacts = mutableListOf<Contact>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        repository = ContactRepository()
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_kontak, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val rvContacts = view.findViewById<RecyclerView>(R.id.rv_contacts)
        adapter = ContactAdapter(contacts) { contact ->
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Kontak")
                .setMessage("Apakah Anda yakin ingin menghapus ${contact.name}?")
                .setPositiveButton("Hapus") { _, _ -> deleteContact(contact.name) }
                .setNegativeButton("Batal", null)
                .show()
        }
        rvContacts.adapter = adapter

        loadContacts()

        // FAB Tambah Kontak
        view.findViewById<View>(R.id.btn_add_contact_top).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_kontak_to_addContactFragment)
        }
    }

    private fun loadContacts() {
        val rawPhone = sessionManager.getUserPhone() ?: ""
        val myPhone = rawPhone.replace(Regex("[^0-9]"), "")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sosRepository = SOSRepository()
                val supabaseContacts = sosRepository.getEmergencyContacts(myPhone)
                contacts.clear()
                contacts.addAll(supabaseContacts)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                // Ignore silent or log
            }
        }
    }

    private fun addContact(contact: Contact) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.insertContact(contact)
                loadContacts()
                Toast.makeText(requireContext(), "Kontak berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteContact(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.deleteContact(name)
                loadContacts()
                Toast.makeText(requireContext(), "Kontak berhasil dihapus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
