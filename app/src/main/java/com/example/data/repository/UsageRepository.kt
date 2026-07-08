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
                    isFavorite = false,
                    favoriteBadge = "Annem"
                ),
                ContactEntity(
                    name = "Babam",
                    phone = "0532 987 11 22",
                    email = "babam@gmail.com",
                    birthday = "15 Ağustos 1968",
                    group = "Aile",
                    isFavorite = false,
                    favoriteBadge = "Babam"
                ),
                ContactEntity(
                    name = "Eşim",
                    phone = "0533 555 44 33",
                    email = "esim@gmail.com",
                    birthday = "12 Eylül 1996",
                    group = "Aile",
                    isFavorite = false,
                    favoriteBadge = "Eşim"
                ),
                ContactEntity(
                    name = "Canım",
                    phone = "0535 777 66 55",
                    email = "canim@gmail.com",
                    group = "Aile",
                    isFavorite = false,
                    favoriteBadge = "Canım"
                ),
                ContactEntity(
                    name = "Ahmet Yılmaz",
                    phone = "0505 123 45 67",
                    email = "ahmetyilmaz@gmail.com",
                    birthday = "15 Mayıs 1995",
                    group = "Arkadaşlar",
                    other = "Zil sesi, Notlar vb.",
                    isFavorite = false,
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
                    isFavorite = false,
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

    suspend fun syncDeviceContactsAndLogs() {
        try {
            val cleanPhoneToIdMap = mutableMapOf<String, Long>()
            
            // 1. Sync Contacts if permission granted
            if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val deviceContacts = fetchDeviceContacts()
                val existingContacts = usageDao.getAllContacts().first()
                val existingPhones = existingContacts.map { cleanPhoneNumber(it.phone) }.toSet()
                
                for (contact in deviceContacts) {
                    val cleanPhone = cleanPhoneNumber(contact.phone)
                    if (cleanPhone.isNotEmpty() && !existingPhones.contains(cleanPhone)) {
                        val contactId = usageDao.insertContact(contact.copy(id = 0))
                        cleanPhoneToIdMap[cleanPhone] = contactId
                    }
                }
            }
            
            // Re-read map of all contacts to link call logs
            val allCurrentContacts = usageDao.getAllContacts().first()
            for (contact in allCurrentContacts) {
                val cleanPhone = cleanPhoneNumber(contact.phone)
                if (cleanPhone.isNotEmpty()) {
                    cleanPhoneToIdMap[cleanPhone] = contact.id
                }
            }
            
            // 2. Sync Call Logs if permission granted
            if (context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val deviceCallLogs = fetchDeviceCallLogs()
                val existingCallLogs = usageDao.getAllCallLogs().first()
                val existingTimestamps = existingCallLogs.map { it.timestamp }.toSet()
                
                for (log in deviceCallLogs) {
                    if (!existingTimestamps.contains(log.timestamp)) {
                        val cleanPhone = cleanPhoneNumber(log.number)
                        val contactId = cleanPhoneToIdMap[cleanPhone]
                        usageDao.insertCallLog(log.copy(id = 0, contactId = contactId))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanPhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "")
    }

    private fun fetchDeviceContacts(): List<ContactEntity> {
        val contactsList = mutableListOf<ContactEntity>()
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null, null
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                
                while (it.moveToNext()) {
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "Bilinmeyen" else "Bilinmeyen"
                    val number = if (numberIdx != -1) it.getString(numberIdx) ?: "" else ""
                    
                    if (number.isNotEmpty()) {
                        contactsList.add(
                            ContactEntity(
                                id = 0,
                                name = name,
                                phone = number,
                                email = "",
                                birthday = "",
                                group = "Cihaz",
                                isFavorite = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contactsList
    }

    private fun fetchDeviceCallLogs(): List<CallLogEntity> {
        val callLogList = mutableListOf<CallLogEntity>()
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                android.provider.CallLog.Calls.CONTENT_URI,
                arrayOf(
                    android.provider.CallLog.Calls.NUMBER,
                    android.provider.CallLog.Calls.CACHED_NAME,
                    android.provider.CallLog.Calls.TYPE,
                    android.provider.CallLog.Calls.DATE,
                    android.provider.CallLog.Calls.DURATION
                ),
                null, null, "${android.provider.CallLog.Calls.DATE} DESC LIMIT 100"
            )
            cursor?.use {
                val numberIdx = it.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)
                val typeIdx = it.getColumnIndex(android.provider.CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(android.provider.CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(android.provider.CallLog.Calls.DURATION)
                
                while (it.moveToNext()) {
                    val number = if (numberIdx != -1) it.getString(numberIdx) ?: "" else ""
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "" else ""
                    val type = if (typeIdx != -1) it.getInt(typeIdx) else -1
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else 0L
                    val duration = if (durationIdx != -1) it.getInt(durationIdx) else 0
                    
                    val callTypeStr = when (type) {
                        android.provider.CallLog.Calls.INCOMING_TYPE -> "Gelen"
                        android.provider.CallLog.Calls.OUTGOING_TYPE -> "Giden"
                        android.provider.CallLog.Calls.MISSED_TYPE -> "Cevapsız"
                        else -> "Gelen"
                    }
                    
                    callLogList.add(
                        CallLogEntity(
                            id = 0,
                            number = number,
                            name = name.ifEmpty { number },
                            callType = callTypeStr,
                            timestamp = date,
                            durationSeconds = duration,
                            category = "Mobil"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return callLogList
    }

    fun launchActualCall(phone: String) {
        val cleanPhone = phone.replace(" ", "")
        val intent = if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.content.Intent(android.content.Intent.ACTION_CALL, android.net.Uri.parse("tel:$cleanPhone"))
        } else {
            android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$cleanPhone"))
        }
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertDeviceContact(name: String, phone: String, email: String) {
        if (context.checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }
        try {
            val contentResolver = context.contentResolver
            val rawContactUri = contentResolver.insert(android.provider.ContactsContract.RawContacts.CONTENT_URI, android.content.ContentValues())
            val rawContactId = rawContactUri?.lastPathSegment?.toLongOrNull() ?: return

            // Insert Name
            val nameValues = android.content.ContentValues().apply {
                put(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            }
            contentResolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, nameValues)

            // Insert Phone
            val phoneValues = android.content.ContentValues().apply {
                put(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                put(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                put(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            }
            contentResolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, phoneValues)

            // Insert Email if present
            if (email.isNotEmpty()) {
                val emailValues = android.content.ContentValues().apply {
                    put(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    put(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                    put(android.provider.ContactsContract.CommonDataKinds.Email.TYPE, android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                }
                contentResolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, emailValues)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
