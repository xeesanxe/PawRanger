package com.example.pawranger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawranger.data.Contact
import com.example.pawranger.data.ContactRepository
import com.example.pawranger.utils.SessionManager
import kotlinx.coroutines.launch

class AddContactFragment : Fragment() {
    private lateinit var repository: ContactRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        repository = ContactRepository()
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_add_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etName = view.findViewById(R.id.et_contact_name)
        etPhone = view.findViewById(R.id.et_contact_phone)

        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.btn_save_contact).setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val name = etName.text.toString().trim()
        val phoneInput = etPhone.text.toString().trim()

        if (name.isEmpty() || phoneInput.isEmpty()) {
            Toast.makeText(requireContext(), "Nama dan Nomor tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val phone = phoneInput.replace(Regex("[^0-9]"), "")
        val rawMyPhone = sessionManager.getUserPhone() ?: ""
        val myPhone = rawMyPhone.replace(Regex("[^0-9]"), "")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // INI YANG DIBENERIN: Pakai named parameter biar mesin nggak bingung bedain id (Int) sama nama (String)
                val newContact = Contact(name = name, phoneNumber = phone, userId = myPhone)

                repository.insertContact(newContact)
                Toast.makeText(requireContext(), "Kontak berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}