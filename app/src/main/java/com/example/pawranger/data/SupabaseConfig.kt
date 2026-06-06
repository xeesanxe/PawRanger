package com.example.pawranger.data

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    // Create the Supabase client and log status; return null if initialization fails
    val client: io.github.jan.supabase.SupabaseClient? = try {
        val c = createSupabaseClient(
            supabaseUrl = "https://sduwrfznesbcowsvvfll.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNkdXdyZnpuZXNiY293c3Z2ZmxsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgwNTI0NjEsImV4cCI6MjA5MzYyODQ2MX0.uhBBh7QXZXPq-wp4pDl5vFniar3J8zHm9uNjppzJp9I"
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
        Log.d("SupabaseConfig", "Supabase client initialized")
        c
    } catch (t: Throwable) {
        // Initialization failed (likely due to missing Ktor plugin classes). Log and return null.
        Log.e("SupabaseConfig", "Failed to initialize Supabase client", t)
        null
    }
}
