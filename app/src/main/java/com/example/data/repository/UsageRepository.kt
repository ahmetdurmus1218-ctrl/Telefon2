package com.example.data.repository

import android.content.Context
import com.example.data.database.CallLogEntity
import com.example.data.database.ContactEntity
import com.example.data.database.UsageDao
import com.example.data.datastore.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UsageRepository(
    private val context: Context,
    private val usageDao: UsageDao,
    private val settingsManager: SettingsManager
) {

    // --- CONTACTS ---
    val allContacts: Flow<List<ContactEntity>> = usageDao.getAllContacts()
    
    val favoriteContacts: Flow<List<ContactEntity>> = usageDao.getFavoriteContacts()

    fun getContactById(id: Long): Flow<ContactEntity?> = usageDao.getContactById(id)

    fun searchContacts(query: String): Flow<List<ContactEntity>> = usageDao.searchContacts(query)

    suspend fun insertContact(contact: ContactEntity): Long {
        return usageDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactEntity) {
        usageDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        usageDao.deleteContact(contact)
    }

    // --- CALL LOGS ---
    val allCallLogs: Flow<List<CallLogEntity>> = usageDao.getAllCallLogs()

    suspend fun insertCallLog(log: CallLogEntity): Long {
        return usageDao.insertCallLog(log)
    }

    suspend fun deleteCallLog(log: CallLogEntity) {
        usageDao.deleteCallLog(log)
    }

    suspend fun clearAllCallLogs() {
        usageDao.clearAllCallLogs()
    }

    // --- PRE-POPULATION ---
    suspend fun prepopulateIfEmpty() {
        // We'll check if we have prepopulated before using a setting, or if contacts is empty
        val contacts = usageDao.getAllContacts().first()
        if (contacts.isEmpty()) {
            val list = listOf(
                ContactEntity(
                    name = "Annem",
                    phone = "0532 111 22 33",
                    email = "annem@gmail.com",
                    birthday = "10 Mayıs 1970",
                    group = "Aile",
                    other = "Zil sesi: Klasik",
                    isFavorite = true,
                    favoriteBadge = "Annem"
                ),
                ContactEntity(
                    name = "Babam",
                    phone = "0532 987 11 22",
                    email = "babam@gmail.com",
                    birthday = "15 Ağustos 1968",
                    group = "Aile",
                    isFavorite = true,
                    favoriteBadge = "Babam"
                ),
                ContactEntity(
                    name = "Eşim",
                    phone = "0533 555 44 33",
                    email = "esim@gmail.com",
                    birthday = "12 Eylül 1996",
                    group = "Aile",
                    isFavorite = true,
                    favoriteBadge = "Eşim"
                ),
                ContactEntity(
                    name = "Canım",
                    phone = "0535 777 66 55",
                    email = "canim@gmail.com",
                    group = "Aile",
                    isFavorite = true,
                    favoriteBadge = "Canım"
                ),
                ContactEntity(
                    name = "Ahmet Yılmaz",
                    phone = "0505 123 45 67",
                    email = "ahmetyilmaz@gmail.com",
                    birthday = "15 Mayıs 1995",
                    group = "Arkadaşlar",
                    other = "Zil sesi, Notlar vb.",
                    isFavorite = true,
                    favoriteBadge = ""
                ),
                ContactEntity(
                    name = "Alperen Demir",
                    phone = "0532 987 65 43",
                    email = "alperen@gmail.com",
                    birthday = "22 Ekim 1994",
                    group = "İş",
                    isFavorite = false
                ),
                ContactEntity(
                    name = "Aslı Kaya",
                    phone = "0506 234 56 78",
                    email = "asli.kaya@gmail.com",
                    birthday = "4 Temmuz 1997",
                    group = "Arkadaşlar",
                    isFavorite = false
                ),
                ContactEntity(
                    name = "Berkay Şahin",
                    phone = "0531 765 43 21",
                    email = "berkay@gmail.com",
                    birthday = "12 Şubat 1993",
                    group = "İş",
                    isFavorite = true,
                    favoriteBadge = ""
                )
            )

            for (contact in list) {
                val id = usageDao.insertContact(contact)
                
                // Add some initial call logs for these contacts
                if (contact.name == "Annem") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Gelen", timestamp = System.currentTimeMillis() - 15 * 60 * 1000, category = "Mobil"))
                } else if (contact.name == "Babam") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Gelen", timestamp = System.currentTimeMillis() - 2 * 3600 * 1000, category = "Mobil"))
                } else if (contact.name == "Eşim") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Cevapsız", timestamp = System.currentTimeMillis() - 24 * 3600 * 1000, category = "Mobil"))
                } else if (contact.name == "Ahmet Yılmaz") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Giden", timestamp = System.currentTimeMillis() - 26 * 3600 * 1000, category = "Mobil"))
                } else if (contact.name == "Alperen Demir") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Gelen", timestamp = System.currentTimeMillis() - 28 * 3600 * 1000, category = "Mobil"))
                } else if (contact.name == "Berkay Şahin") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Giden", timestamp = System.currentTimeMillis() - 2 * 24 * 3600 * 1000, category = "Mobil"))
                } else if (contact.name == "Aslı Kaya") {
                    usageDao.insertCallLog(CallLogEntity(contactId = id, name = contact.name, number = contact.phone, callType = "Gelen", timestamp = System.currentTimeMillis() - 3 * 24 * 3600 * 1000, category = "Mobil"))
                }
            }
        }
    }
}
