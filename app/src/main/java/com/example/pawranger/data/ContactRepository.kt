package com.example.pawranger.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getContacts(): List<Contact> {
        val userId = auth.currentUser?.uid ?: return emptyList()

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
        val userId = auth.currentUser?.uid

        val data = hashMapOf(
            "name" to contact.name,
            "phoneNumber" to contact.phoneNumber,
            "userId" to userId
        )

        db.collection("contacts")
            .add(data)
            .await()
    }

    suspend fun deleteContact(name: String) {
        val userId = auth.currentUser?.uid ?: return

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