package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.CallLogEntity
import com.example.data.database.ContactEntity
import com.example.data.datastore.SettingsManager
import com.example.data.repository.UsageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface RehberUiState {
    object Loading : RehberUiState
    data class Success(
        val contacts: List<ContactEntity>,
        val favorites: List<ContactEntity>,
        val callLogs: List<CallLogEntity>
    ) : RehberUiState
}

class ScreenPulseViewModel(
    private val repository: UsageRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dialpadInput = MutableStateFlow("")
    val dialpadInput: StateFlow<String> = _dialpadInput.asStateFlow()

    private val _selectedCallFilter = MutableStateFlow("Tümü") // "Tümü", "Cevapsız", "Gelen", "Giden"
    val selectedCallFilter: StateFlow<String> = _selectedCallFilter.asStateFlow()

    // Observe all contacts from the repository
    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe favorite contacts
    val favoriteContacts: StateFlow<List<ContactEntity>> = repository.favoriteContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe call logs
    val allCallLogs: StateFlow<List<CallLogEntity>> = repository.allCallLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            repository.syncDeviceContactsAndLogs()
        }
    }

    fun syncDeviceData() {
        viewModelScope.launch {
            repository.syncDeviceContactsAndLogs()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCallFilter(filter: String) {
        _selectedCallFilter.value = filter
    }

    // --- DIALPAD OPERATIONS ---
    fun appendToDialpad(char: Char) {
        _dialpadInput.value = _dialpadInput.value + char
    }

    fun backspaceDialpad() {
        if (_dialpadInput.value.isNotEmpty()) {
            _dialpadInput.value = _dialpadInput.value.dropLast(1)
        }
    }

    fun clearDialpad() {
        _dialpadInput.value = ""
    }

    // --- CONTACT OPERATIONS ---
    fun addContact(name: String, phone: String, email: String, birthday: String, group: String, other: String, isFavorite: Boolean, favoriteBadge: String, storageAccount: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertContact(
                ContactEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    birthday = birthday,
                    group = group,
                    other = other,
                    isFavorite = isFavorite,
                    favoriteBadge = favoriteBadge,
                    storageAccount = storageAccount
                )
            )
            repository.insertDeviceContact(name, phone, email)
            repository.syncDeviceContactsAndLogs()
            onComplete()
        }
    }

    fun updateContact(contact: ContactEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateContact(contact)
            onComplete()
        }
    }

    fun toggleFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            repository.updateContact(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun deleteContact(contact: ContactEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            onComplete()
        }
    }

    // --- CALL LOG OPERATIONS ---
    fun makeCall(phone: String, name: String = "", contactId: Long? = null) {
        viewModelScope.launch {
            repository.insertCallLog(
                CallLogEntity(
                    contactId = contactId,
                    number = phone,
                    name = name.ifEmpty { phone },
                    callType = "Giden",
                    timestamp = System.currentTimeMillis()
                )
            )
            repository.launchActualCall(phone)
            clearDialpad()
        }
    }

    fun startSimulatedCall(context: android.content.Context, phone: String, name: String, type: String, delaySeconds: Int = 0) {
        viewModelScope.launch {
            if (delaySeconds > 0) {
                delay(delaySeconds * 1000L)
            }
            val intent = android.content.Intent(context, com.example.CallActivity::class.java).apply {
                putExtra("is_simulation", true)
                putExtra("sim_number", phone)
                putExtra("sim_name", name.ifEmpty { phone })
                putExtra("sim_type", type)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    fun addMissedCall(phone: String, name: String = "", contactId: Long? = null) {
        viewModelScope.launch {
            repository.insertCallLog(
                CallLogEntity(
                    contactId = contactId,
                    number = phone,
                    name = name.ifEmpty { phone },
                    callType = "Cevapsız",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteCallLog(log: CallLogEntity) {
        viewModelScope.launch {
            repository.deleteCallLog(log)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            repository.clearAllCallLogs()
        }
    }

    // Factory pattern
    class Factory(
        private val repository: UsageRepository,
        private val settingsManager: SettingsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScreenPulseViewModel::class.java)) {
                return ScreenPulseViewModel(repository, settingsManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
