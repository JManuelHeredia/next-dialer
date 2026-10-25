package com.nextdialer

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nextdialer.data.CallEntry
import com.nextdialer.data.Contact
import com.nextdialer.data.ContactDetail
import com.nextdialer.data.ContactNumber
import com.nextdialer.data.DeviceRepository
import com.nextdialer.util.PhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeviceRepository(application)
    private val resolver = application.contentResolver
    private val settingsStore = (application as NextDialerApp).settings

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _calls = MutableStateFlow<List<CallEntry>>(emptyList())
    val calls: StateFlow<List<CallEntry>> = _calls.asStateFlow()

    private val _contactsGranted = MutableStateFlow(false)
    val contactsGranted: StateFlow<Boolean> = _contactsGranted.asStateFlow()

    private val _callLogGranted = MutableStateFlow(false)
    val callLogGranted: StateFlow<Boolean> = _callLogGranted.asStateFlow()

    private val _phoneGranted = MutableStateFlow(false)
    val phoneGranted: StateFlow<Boolean> = _phoneGranted.asStateFlow()

    private val _writeCallLogGranted = MutableStateFlow(false)
    val writeCallLogGranted: StateFlow<Boolean> = _writeCallLogGranted.asStateFlow()

    private var reloadJob: Job? = null
    private var observing = false

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            scheduleReload()
        }
    }

    init {
        refreshPermissions()
        reloadNow()
    }

    fun refreshPermissions() {
        val contacts = hasPermission(Manifest.permission.READ_CONTACTS)
        val callLog = hasPermission(Manifest.permission.READ_CALL_LOG)
        val writeCallLog = hasPermission(Manifest.permission.WRITE_CALL_LOG)
        val phone = hasPermission(Manifest.permission.CALL_PHONE)

        val changed = contacts != _contactsGranted.value ||
            callLog != _callLogGranted.value ||
            writeCallLog != _writeCallLogGranted.value ||
            phone != _phoneGranted.value

        _contactsGranted.value = contacts
        _callLogGranted.value = callLog
        _writeCallLogGranted.value = writeCallLog
        _phoneGranted.value = phone

        updateObserver(contacts || callLog)
        if (changed) reloadNow()
    }

    private fun updateObserver(shouldObserve: Boolean) {
        if (shouldObserve == observing) return
        runCatching {
            if (shouldObserve) {
                resolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI,
                    true,
                    observer,
                )
                resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
            } else {
                resolver.unregisterContentObserver(observer)
            }
        }
        observing = shouldObserve
    }

    fun reload() {
        reloadNow()
    }

    private fun scheduleReload() {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            delay(250)
            performReload()
        }
    }

    private fun reloadNow() {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch { performReload() }
    }

    private suspend fun performReload() {
        val contacts = if (_contactsGranted.value) {
            withContext(Dispatchers.IO) {
                runCatching { repository.loadContacts() }.getOrDefault(emptyList())
            }
        } else {
            emptyList()
        }
        val calls = if (_callLogGranted.value) {
            val state = settingsStore.state.value
            withContext(Dispatchers.IO) {
                runCatching {
                    repository.loadCalls(contacts, state.mergeBy, state.callLogRangeDays)
                }.getOrDefault(emptyList())
            }
        } else {
            emptyList()
        }
        _contacts.value = contacts
        _calls.value = calls
    }

    fun deleteCalls(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val removed = withContext(Dispatchers.IO) {
                runCatching { repository.deleteCalls(ids) }.getOrDefault(false)
            }
            if (!removed) {
                Toast.makeText(
                    getApplication(),
                    "Call history is read-only. Allow access in Android settings to delete calls.",
                    Toast.LENGTH_LONG,
                ).show()
            }
            reloadNow()
        }
    }

    fun deleteCallsForNumber(number: String) {
        val ids = _calls.value
            .filter { PhoneUtils.numbersMatch(it.number, number) }
            .flatMap { it.ids }
            .distinct()
        if (ids.isEmpty()) {
            Toast.makeText(
                getApplication(),
                "No calls found for $number.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        deleteCalls(ids)
    }

    fun clearCallLog() {
        viewModelScope.launch {
            val removed = withContext(Dispatchers.IO) {
                runCatching { repository.clearCallLog() }.getOrDefault(false)
            }
            if (!removed) {
                Toast.makeText(
                    getApplication(),
                    "Call history is read-only. Allow access in Android settings to clear it.",
                    Toast.LENGTH_LONG,
                ).show()
            }
            reloadNow()
        }
    }

    suspend fun contactDetail(contactId: Long): ContactDetail? =
        withContext(Dispatchers.IO) {
            runCatching { repository.loadDetail(contactId) }.getOrNull()
        }

    /**
     * Detail model for a number, whether or not it belongs to a stored contact. Used by the
     * per-number screen that opens from call history and contacts.
     */
    suspend fun detailFor(number: String, contactId: Long?): ContactDetail {
        val match = contactId?.let { id -> _contacts.value.firstOrNull { it.id == id } }
            ?: _contacts.value.firstOrNull { contact ->
                contact.numbers.any { PhoneUtils.numbersMatch(it.number, number) }
            }
        if (match != null) {
            val loaded = contactDetail(match.id)
            if (loaded != null) return loaded
        }
        val normalized = PhoneUtils.normalize(number)
        val cachedName = _calls.value
            .firstOrNull { PhoneUtils.numbersMatch(it.number, number) }
            ?.displayName
        return ContactDetail(
            contact = Contact(
                id = 0L,
                lookupKey = "",
                name = cachedName ?: number,
                photoUri = null,
                numbers = listOf(
                    ContactNumber(
                        number = number,
                        normalized = normalized,
                        type = 0,
                        label = null,
                        typeLabel = "Phone",
                    ),
                ),
                starred = false,
                sortKey = "",
            ),
            emails = emptyList(),
        )
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            getApplication(),
            permission,
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCleared() {
        runCatching { resolver.unregisterContentObserver(observer) }
        super.onCleared()
    }
}
