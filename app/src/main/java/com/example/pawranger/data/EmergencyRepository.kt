package com.example.pawranger.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EmergencyRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getEmergencyContacts(): List<EmergencyContact> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = db.collection("emergency_contacts")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            EmergencyContact(
                id = null,
                userId = doc.getString("userId"),
                contactName = doc.getString("contactName") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                relationship = doc.getString("relationship"),
                createdAt = doc.getString("createdAt")
            )
        }
    }

    suspend fun insertEmergencyContact(emergencyContact: EmergencyContact) {
        val userId = auth.currentUser?.uid

        val data = hashMapOf(
            "userId" to userId,
            "contactName" to emergencyContact.contactName,
            "phoneNumber" to emergencyContact.phoneNumber,
            "relationship" to emergencyContact.relationship,
            "createdAt" to System.currentTimeMillis().toString()
        )

        db.collection("emergency_contacts")
            .add(data)
            .await()
    }

    suspend fun deleteEmergencyContact(phoneNumber: String) {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = db.collection("emergency_contacts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .await()

        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }
}