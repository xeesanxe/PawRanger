package com.example.pawranger.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PawRanger.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CONTACTS = "contacts"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun addContact(contact: Contact): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, contact.name)
        values.put(KEY_PHONE, contact.phoneNumber)
        return db.insert(TABLE_CONTACTS, null, values)
    }

    fun getAllContacts(): MutableList<Contact> {
        val contactsList = mutableListOf<Contact>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CONTACTS", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE))
                contactsList.add(Contact(name, phone))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contactsList
    }

    fun deleteContact(name: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CONTACTS, "$KEY_NAME=?", arrayOf(name))
    }
}