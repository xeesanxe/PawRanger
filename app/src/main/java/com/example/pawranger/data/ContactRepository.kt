package com.example.pawranger.data

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository {
    private val client = SupabaseConfig.client

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        client.postgrest.from("contacts").select().decodeList<Contact>()
    }

    suspend fun insertContact(contact: Contact): Unit = withContext(Dispatchers.IO) {
        // Harus menggunakan listOf() untuk Postgrest v2.x
        client.postgrest.from("contacts").insert(listOf(contact))
    }

    suspend fun deleteContact(name: String): Unit = withContext(Dispatchers.IO) {
        client.postgrest.from("contacts").delete {
            filter {
                eq("name", name)
            }
        }
    }
}
