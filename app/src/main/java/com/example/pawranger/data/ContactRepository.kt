package com.example.pawranger.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getContacts(): List<Contact> {
        // Kita prioritaskan pakai ID dari Firebase Auth jika ada, 
        // tapi sistem kita saat ini pakai Nomor HP sebagai userId
        val userId = auth.currentUser?.uid ?: ""

        val snapshot = db.collection("contacts")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Contact(
                id = 0,
                name = doc.getString("name") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                userId = doc.getString("userId")
            )
        }
    }

    suspend fun insertContact(contact: Contact) {
        // Gunakan userId yang dikirim dari Fragment (biasanya nomor HP user)
        // Jika contact.userId kosong, baru coba ambil dari Firebase Auth
        val finalUserId = if (!contact.userId.isNullOrEmpty()) {
            contact.userId
        } else {
            auth.currentUser?.uid
        }

        val data = hashMapOf(
            "name" to contact.name,
            "phoneNumber" to contact.phoneNumber,
            "userId" to finalUserId
        )

        db.collection("contacts")
            .add(data)
            .await()
    }

    suspend fun deleteContact(name: String, userId: String) {
        val snapshot = db.collection("contacts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", name)
            .get()
            .await()

        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }
}
