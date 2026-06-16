package com.example.pawranger.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseTest {

    fun testFirestore() {

        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "message" to "Firebase Berhasil",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("test")
            .add(data)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Data berhasil masuk")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "Gagal", it)
            }
    }
}