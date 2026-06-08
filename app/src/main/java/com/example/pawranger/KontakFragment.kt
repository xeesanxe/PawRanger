package com.example.pawranger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
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
            deleteContact(contact.name)
        }
        rvContacts.adapter = adapter

        loadContacts()

        // Tombol Tambah
        view.findViewById<View>(R.id.btn_add_contact_top).setOnClickListener {
            showAddContactDialog()
        }

        view.findViewById<View>(R.id.iv_profile_top)?.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
        }

        view.findViewById<View>(R.id.fab_emergency)?.setOnClickListener {
            Toast.makeText(requireContext(), "DARURAT! Sinyal SOS dikirim.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_contact_name)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.et_contact_phone)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString()
                val phone = etPhone.text.toString().replace(Regex("[^0-9]"), "")
                val rawPhone = sessionManager.getUserPhone() ?: ""
                val myPhone = rawPhone.replace(Regex("[^0-9]"), "")

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    val newContact = Contact(name, phone, myPhone)
                    addContact(newContact)
                } else {
                    Toast.makeText(requireContext(), "Nama dan Nomor tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadContacts() {
        val rawPhone = sessionManager.getUserPhone() ?: ""
        val myPhone = rawPhone.replace(Regex("[^0-9]"), "")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Ambil repository yang benar
                val sosRepository = SOSRepository()
                val supabaseContacts = sosRepository.getEmergencyContacts(myPhone)
                contacts.clear()
                contacts.addAll(supabaseContacts)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addContact(contact: Contact) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.insertContact(contact)
                loadContacts()
                Toast.makeText(requireContext(), "Kontak berhasil disimpan ke Cloud", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Kontak dihapus dari Cloud", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}