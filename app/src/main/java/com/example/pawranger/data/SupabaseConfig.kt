package com.example.pawranger.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    val client = createSupabaseClient(
        supabaseUrl = "https://sduwrfznesbcowsvvfll.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNkdXdyZnpuZXNiY293c3Z2ZmxsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgwNTI0NjEsImV4cCI6MjA5MzYyODQ2MX0.uhBBh7QXZXPq-wp4pDl5vFniar3J8zHm9uNjppzJp9I",
    ) {
        install(Postgrest)
        install(Realtime)
    }
}
