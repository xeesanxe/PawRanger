package com.example.pawranger.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pawranger.R
import com.example.pawranger.data.Contact
import com.example.pawranger.data.DatabaseHelper
import com.example.pawranger.ui.adapter.ContactAdapter
import com.google.android.material.textfield.TextInputEditText

class KontakFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ContactAdapter
    private var contacts = mutableListOf<Contact>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = DatabaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_kontak, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadContacts()

        val rvContacts = view.findViewById<RecyclerView>(R.id.rv_contacts)
        adapter = ContactAdapter(contacts) { contact ->
            dbHelper.deleteContact(contact.name)
            loadContacts()
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Kontak dihapus", Toast.LENGTH_SHORT).show()
        }
        rvContacts.adapter = adapter

        // Tombol Tambah di Kiri Atas
        view.findViewById<ImageButton>(R.id.btn_add_contact_top).setOnClickListener {
            showAddContactDialog()
        }

        // Tombol Back di Kanan Atas
        view.findViewById<ImageButton>(R.id.btn_back_kontak).setOnClickListener {
            findNavController().navigateUp()
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
                val phone = etPhone.text.toString()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    val newContact = Contact(name, phone)
                    dbHelper.addContact(newContact)
                    loadContacts()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Kontak berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Nama dan Nomor tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadContacts() {
        val dbContacts = dbHelper.getAllContacts()
        contacts.clear()
        contacts.addAll(dbContacts)
    }
}