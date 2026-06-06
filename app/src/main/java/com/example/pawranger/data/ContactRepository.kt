package com.example.pawranger.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository {
    private val client = SupabaseConfig.client

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        client!!.postgrest.from("contacts")
            .select()
            .decodeList<Contact>()
    }

    suspend fun insertContact(contact: Contact): Unit = withContext(Dispatchers.IO) {
        // Ambil user_id dari session login yang sedang aktif
        val userId = client!!.auth.currentSessionOrNull()?.user?.id

        val contactWithUser = contact.copy(userId = userId)
        client.postgrest.from("contacts")
            .insert(contactWithUser)
    }

    suspend fun deleteContact(name: String): Unit = withContext(Dispatchers.IO) {
        client!!.postgrest.from("contacts").delete {
            filter {
                eq("name", name)
            }
        }
    }
}