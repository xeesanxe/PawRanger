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
import com.example.pawranger.adapter.ContactAdapter
import androidx.lifecycle.lifecycleScope
import com.example.pawranger.data.ContactRepository
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.utils.PhoneUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class KontakFragment : Fragment() {
    private lateinit var adapter: ContactAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: ContactRepository
    private var contacts = mutableListOf<Contact>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        repository = ContactRepository()
        return inflater.inflate(R.layout.fragment_kontak, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvContacts = view.findViewById<RecyclerView>(R.id.rv_contacts)

        adapter = ContactAdapter(contacts) { contact ->
            showDeleteConfirmDialog(contact)
        }
        rvContacts.adapter = adapter

        loadContacts()

        val btnAddTop = view.findViewById<View>(R.id.btn_add_contact_top)
        btnAddTop?.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_kontak_to_addContactFragment)
        }
    }

    private fun showDeleteConfirmDialog(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kontak")
            .setMessage("Apakah kamu yakin ingin menghapus ${contact.name}?")
            .setPositiveButton("Hapus") { _, _ -> deleteContact(contact.name) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadContacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val rawMyPhone = sessionManager.getUserPhone() ?: ""
                val myPhone = PhoneUtils.formatPhoneNumber(rawMyPhone)

                if (myPhone.isNotEmpty()) {
                    val snapshot = FirebaseFirestore.getInstance().collection("contacts")
                        .whereEqualTo("userId", myPhone)
                        .get()
                        .await()

                    val firebaseContacts = snapshot.toObjects(Contact::class.java)

                    contacts.clear()
                    if (firebaseContacts.isNotEmpty()) {
                        contacts.addAll(firebaseContacts)
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat data kontak: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteContact(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val rawMyPhone = sessionManager.getUserPhone() ?: ""
                val myPhone = PhoneUtils.formatPhoneNumber(rawMyPhone)

                repository.deleteContact(name, myPhone)

                loadContacts()
                Toast.makeText(requireContext(), "Kontak berhasil dihapus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menghapus kontak: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
