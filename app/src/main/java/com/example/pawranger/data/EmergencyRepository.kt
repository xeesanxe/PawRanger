package com.example.pawranger.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencyRepository {

    private val client = SupabaseConfig.client

    suspend fun getEmergencyContacts(): List<EmergencyContact> =
        withContext(Dispatchers.IO) {

            client!!.postgrest
                .from("emergency_contacts")
                .select()
                .decodeList<EmergencyContact>()
        }

    suspend fun insertEmergencyContact(
        emergencyContact: EmergencyContact
    ): Unit = withContext(Dispatchers.IO) {

        val userId =
            client!!.auth.currentSessionOrNull()?.user?.id

        val contactWithUser =
            emergencyContact.copy(userId = userId)

        client.postgrest
            .from("emergency_contacts")
            .insert(contactWithUser)
    }

    suspend fun deleteEmergencyContact(
        phoneNumber: String
    ): Unit = withContext(Dispatchers.IO) {

        client!!.postgrest
            .from("emergency_contacts")
            .delete {
                filter {
                    eq("phone_number", phoneNumber)
                }
            }
    }
}