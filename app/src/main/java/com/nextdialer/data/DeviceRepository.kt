package com.nextdialer.data

import android.content.ContentResolver
import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.RawContacts
import com.nextdialer.util.PhoneUtils

class DeviceRepository(context: Context) {

    private val resolver: ContentResolver = context.applicationContext.contentResolver
    private val resources = context.applicationContext.resources

    fun loadContactSources(): List<ContactSource> {
        val projection = arrayOf(
            RawContacts.CONTACT_ID,
            RawContacts.ACCOUNT_NAME,
            RawContacts.ACCOUNT_TYPE,
        )
        val idsByKey = LinkedHashMap<String, MutableSet<Long>>()
        val info = HashMap<String, ContactSource>()
        runCatching {
            resolver.query(
                RawContacts.CONTENT_URI,
                projection,
                "${RawContacts.DELETED} = 0",
                null,
                null,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(RawContacts.CONTACT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(RawContacts.ACCOUNT_NAME)
                val typeIndex = cursor.getColumnIndexOrThrow(RawContacts.ACCOUNT_TYPE)
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(idIndex)
                    if (contactId <= 0L) continue
                    val name = cursor.getString(nameIndex)
                    val type = cursor.getString(typeIndex)
                    val key = sourceKey(type, name)
                    idsByKey.getOrPut(key) { LinkedHashSet() }.add(contactId)
                    if (key !in info) {
                        val (label, subtitle) = sourceInfo(type, name)
                        info[key] = ContactSource(key, label, subtitle, 0)
                    }
                }
            }
        }
        return idsByKey.map { (key, ids) ->
            info.getValue(key).copy(contactCount = ids.size)
        }.sortedWith(
            compareBy<ContactSource>(
                { when (it.key) { "phone" -> 0; "sim" -> 1; else -> 2 } },
                { it.label.lowercase() },
            ),
        )
    }

    private fun allowedContactIds(sources: Set<String>): Set<Long> {
        val allowed = HashSet<Long>()
        val projection = arrayOf(
            RawContacts.CONTACT_ID,
            RawContacts.ACCOUNT_NAME,
            RawContacts.ACCOUNT_TYPE,
        )
        runCatching {
            resolver.query(
                RawContacts.CONTENT_URI,
                projection,
                "${RawContacts.DELETED} = 0",
                null,
                null,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(RawContacts.CONTACT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(RawContacts.ACCOUNT_NAME)
                val typeIndex = cursor.getColumnIndexOrThrow(RawContacts.ACCOUNT_TYPE)
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(idIndex)
                    val key = sourceKey(
                        cursor.getString(typeIndex),
                        cursor.getString(nameIndex),
                    )
                    if (key in sources) allowed.add(contactId)
                }
            }
        }
        return allowed
    }

    private fun sourceKey(type: String?, name: String?): String = when {
        type.isNullOrBlank() -> "phone"
        type.contains("sim", ignoreCase = true) -> "sim"
        else -> "account:$type|${name.orEmpty()}"
    }

    private fun sourceInfo(type: String?, name: String?): Pair<String, String> = when {
        type.isNullOrBlank() -> "Phone" to "Device storage"
        type.contains("sim", ignoreCase = true) -> "SIM" to (name ?: "SIM card")
        else -> (name?.takeIf { it.isNotBlank() } ?: "Account") to (type ?: "Account")
    }

    fun loadContacts(enabledSources: Set<String> = setOf("*")): List<Contact> {
        val allowedIds = when {
            enabledSources.contains("*") -> null
            enabledSources.contains(NONE_SOURCE) -> return emptyList()
            else -> allowedContactIds(enabledSources)
        }
        val organizations = loadOrganizations()
        val projection = arrayOf(
            Phone.CONTACT_ID,
            Phone.LOOKUP_KEY,
            Phone.DISPLAY_NAME_PRIMARY,
            Phone.NUMBER,
            Phone.NORMALIZED_NUMBER,
            Phone.TYPE,
            Phone.LABEL,
            Phone.PHOTO_THUMBNAIL_URI,
            Phone.PHOTO_URI,
            Phone.STARRED,
            Phone.SORT_KEY_PRIMARY,
        )
        val builders = LinkedHashMap<Long, ContactBuilder>()

        runCatching {
            resolver.query(
                Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${Phone.SORT_KEY_PRIMARY} COLLATE LOCALIZED ASC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)
                val lookupIndex = cursor.getColumnIndexOrThrow(Phone.LOOKUP_KEY)
                val nameIndex = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME_PRIMARY)
                val numberIndex = cursor.getColumnIndexOrThrow(Phone.NUMBER)
                val normalizedIndex = cursor.getColumnIndexOrThrow(Phone.NORMALIZED_NUMBER)
                val typeIndex = cursor.getColumnIndexOrThrow(Phone.TYPE)
                val labelIndex = cursor.getColumnIndexOrThrow(Phone.LABEL)
                val thumbIndex = cursor.getColumnIndexOrThrow(Phone.PHOTO_THUMBNAIL_URI)
                val photoIndex = cursor.getColumnIndexOrThrow(Phone.PHOTO_URI)
                val starredIndex = cursor.getColumnIndexOrThrow(Phone.STARRED)
                val sortIndex = cursor.getColumnIndexOrThrow(Phone.SORT_KEY_PRIMARY)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    if (allowedIds != null && id !in allowedIds) continue
                    val number = cursor.getString(numberIndex) ?: continue
                    val normalized = cursor.getString(normalizedIndex)
                        ?: PhoneUtils.normalize(number)
                    val type = cursor.getInt(typeIndex)
                    val label = cursor.getString(labelIndex)
                    val typeLabel = Phone.getTypeLabel(resources, type, label).toString()
                    val builder = builders.getOrPut(id) {
                        ContactBuilder(
                            id = id,
                            lookupKey = cursor.getString(lookupIndex) ?: "",
                            name = cursor.getString(nameIndex) ?: number,
                            photoUri = cursor.getString(thumbIndex)
                                ?: cursor.getString(photoIndex),
                            starred = cursor.getInt(starredIndex) == 1,
                            sortKey = cursor.getString(sortIndex) ?: "",
                            organization = organizations[cursor.getLong(idIndex)],
                        )
                    }
                    builder.add(
                        ContactNumber(
                            number = number,
                            normalized = normalized,
                            type = type,
                            label = label,
                            typeLabel = typeLabel,
                        ),
                    )
                }
            }
        }

        return builders.values.map { it.build() }
    }

    fun loadCalls(contacts: List<Contact>, mergeBy: Int = 2, rangeDays: Int = 0): List<CallEntry> {
        val byNumber = HashMap<String, Contact>()
        val byTail = HashMap<String, Contact>()
        for (contact in contacts) {
            for (number in contact.numbers) {
                if (number.normalized.isNotEmpty()) byNumber[number.normalized] = contact
                if (number.normalized.length >= 7) {
                    byTail[number.normalized.takeLast(7)] = contact
                }
            }
        }

        fun match(number: String): Contact? {
            val digits = PhoneUtils.normalize(number)
            if (digits.isEmpty()) return null
            byNumber[digits]?.let { return it }
            if (digits.length >= 7) byTail[digits.takeLast(7)]?.let { return it }
            return null
        }

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_TYPE,
            CallLog.Calls.CACHED_NUMBER_LABEL,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.NEW,
            CallLog.Calls.CACHED_PHOTO_URI,
        )
        val result = ArrayList<CallEntry>()
        val selection = if (rangeDays > 0) {
            "${CallLog.Calls.DATE} >= ?"
        } else {
            null
        }
        val selectionArgs = if (rangeDays > 0) {
            arrayOf(
                (System.currentTimeMillis() - rangeDays * 24L * 60L * 60L * 1000L).toString(),
            )
        } else {
            null
        }

        runCatching {
            resolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val typeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NUMBER_TYPE)
                val labelIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NUMBER_LABEL)
                val dateIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val callTypeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val newIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NEW)
                val photoIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI)

                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex) ?: ""
                    val contact = match(number)
                    val cachedType = cursor.getInt(typeIndex)
                    val cachedLabel = cursor.getString(labelIndex)
                    val matchedNumber = contact?.numbers?.firstOrNull {
                        PhoneUtils.numbersMatch(it.number, number)
                    }
                    val label = matchedNumber?.typeLabel
                        ?: if (cachedType > 0) {
                            Phone.getTypeLabel(resources, cachedType, cachedLabel).toString()
                        } else {
                            null
                        }
                    val cachedName = cursor.getString(nameIndex)
                    result.add(
                        CallEntry(
                            id = cursor.getLong(idIndex),
                            number = number,
                            displayName = contact?.name
                                ?: cachedName?.takeIf { it.isNotBlank() && it != number },
                            photoUri = contact?.photoUri ?: cursor.getString(photoIndex),
                            date = cursor.getLong(dateIndex),
                            duration = cursor.getLong(durationIndex),
                            kind = callKind(cursor.getInt(callTypeIndex)),
                            isNew = cursor.getInt(newIndex) == 1,
                            contactId = contact?.id,
                            numberLabel = label,
                            organization = contact?.organization,
                        ),
                    )
                }
            }
        }

        return mergeCalls(result, mergeBy)
    }

    /**
     * The original exDialer "merge items by" preference: 0 = keep every call, 1 = one row per
     * person, 2 = one row per phone number. The newest call represents the row and the others
     * only add to its count (and to its unread-missed tally).
     */
    private fun mergeCalls(calls: List<CallEntry>, mergeBy: Int): List<CallEntry> {
        if (mergeBy != 1 && mergeBy != 2) return calls

        val merged = LinkedHashMap<String, CallEntry>()
        for (call in calls) {
            val key = when (mergeBy) {
                1 -> call.contactId?.let { "contact:$it" }
                    ?: "number:${PhoneUtils.normalize(call.number)}"
                else -> "number:${PhoneUtils.normalize(call.number)}"
            }
            val existing = merged[key]
            if (existing == null) {
                merged[key] = call
            } else {
                merged[key] = existing.copy(
                    count = existing.count + 1,
                    ids = existing.ids + call.id,
                    unreadIds = existing.unreadIds + call.unreadIds,
                )
            }
        }
        return merged.values.toList()
    }

    /** Deletes the given call-log rows. Returns true when at least one row was removed. */
    fun deleteCalls(ids: List<Long>): Boolean {
        if (ids.isEmpty()) return false
        val placeholders = ids.joinToString(",") { "?" }
        val removed = runCatching {
            resolver.delete(
                CallLog.Calls.CONTENT_URI,
                "${CallLog.Calls._ID} IN ($placeholders)",
                ids.map { it.toString() }.toTypedArray(),
            )
        }.getOrDefault(0)
        return removed > 0
    }

    /** Deletes the whole call log. Returns true when at least one row was removed. */
    fun clearCallLog(): Boolean {
        val removed = runCatching {
            resolver.delete(CallLog.Calls.CONTENT_URI, null, null)
        }.getOrDefault(0)
        return removed > 0
    }

    fun loadDetail(contactId: Long): ContactDetail? {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.SORT_KEY_PRIMARY,
        )

        var contact: Contact? = null
        runCatching {
            resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                "${ContactsContract.Contacts._ID} = ?",
                arrayOf(contactId.toString()),
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    )
                    val photoIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    )
                    val fullPhotoIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.PHOTO_URI,
                    )
                    val starredIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.STARRED,
                    )
                    val sortIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.SORT_KEY_PRIMARY,
                    )
                    val lookupIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.LOOKUP_KEY,
                    )
                    contact = Contact(
                        id = contactId,
                        lookupKey = cursor.getString(lookupIndex) ?: "",
                        name = cursor.getString(nameIndex) ?: "",
                        photoUri = cursor.getString(photoIndex) ?: cursor.getString(fullPhotoIndex),
                        numbers = loadNumbers(contactId),
                        starred = cursor.getInt(starredIndex) == 1,
                        sortKey = cursor.getString(sortIndex) ?: "",
                        organization = loadOrganization(contactId),
                    )
                }
            }
        }

        val found = contact ?: return null
        val emails = loadEmails(contactId)
        return ContactDetail(
            contact = found.copy(
                name = found.name.ifBlank {
                    found.numbers.firstOrNull()?.number ?: "Unknown"
                },
            ),
            emails = emails,
        )
    }

    private fun loadOrganizations(): Map<Long, String> {
        val projection = arrayOf(Organization.CONTACT_ID, Organization.COMPANY)
        val companies = HashMap<Long, String>()
        runCatching {
            resolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                "${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(Organization.CONTENT_ITEM_TYPE),
                null,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Organization.CONTACT_ID)
                val companyIndex = cursor.getColumnIndexOrThrow(Organization.COMPANY)
                while (cursor.moveToNext()) {
                    val company = cursor.getString(companyIndex)
                        ?.takeIf { it.isNotBlank() }
                        ?: continue
                    companies.putIfAbsent(cursor.getLong(idIndex), company)
                }
            }
        }
        return companies
    }

    private fun loadOrganization(contactId: Long): String? {
        val projection = arrayOf(Organization.COMPANY)
        var company: String? = null
        runCatching {
            resolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                "${Organization.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId.toString(), Organization.CONTENT_ITEM_TYPE),
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    company = cursor.getString(
                        cursor.getColumnIndexOrThrow(Organization.COMPANY),
                    )?.takeIf { it.isNotBlank() }
                }
            }
        }
        return company
    }

    private fun loadNumbers(contactId: Long): List<ContactNumber> {
        val projection = arrayOf(
            Phone.NUMBER,
            Phone.NORMALIZED_NUMBER,
            Phone.TYPE,
            Phone.LABEL,
        )
        val numbers = LinkedHashMap<String, ContactNumber>()
        runCatching {
            resolver.query(
                Phone.CONTENT_URI,
                projection,
                "${Phone.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null,
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndexOrThrow(Phone.NUMBER)
                val normalizedIndex = cursor.getColumnIndexOrThrow(Phone.NORMALIZED_NUMBER)
                val typeIndex = cursor.getColumnIndexOrThrow(Phone.TYPE)
                val labelIndex = cursor.getColumnIndexOrThrow(Phone.LABEL)
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex) ?: continue
                    val normalized = cursor.getString(normalizedIndex)
                        ?: PhoneUtils.normalize(number)
                    val type = cursor.getInt(typeIndex)
                    val label = cursor.getString(labelIndex)
                    val key = normalized.ifEmpty { number }
                    numbers.putIfAbsent(
                        key,
                        ContactNumber(
                            number = number,
                            normalized = normalized,
                            type = type,
                            label = label,
                            typeLabel = Phone.getTypeLabel(resources, type, label).toString(),
                        ),
                    )
                }
            }
        }
        return numbers.values.toList()
    }

    private fun loadEmails(contactId: Long): List<String> {
        val projection = arrayOf(Email.ADDRESS)
        val emails = LinkedHashSet<String>()
        runCatching {
            resolver.query(
                Email.CONTENT_URI,
                projection,
                "${Email.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null,
            )?.use { cursor ->
                val addressIndex = cursor.getColumnIndexOrThrow(Email.ADDRESS)
                while (cursor.moveToNext()) {
                    cursor.getString(addressIndex)
                        ?.takeIf { it.isNotBlank() }
                        ?.let(emails::add)
                }
            }
        }
        return emails.toList()
    }

    private fun callKind(type: Int): CallKind = when (type) {
        CallLog.Calls.INCOMING_TYPE -> CallKind.INCOMING
        CallLog.Calls.OUTGOING_TYPE -> CallKind.OUTGOING
        CallLog.Calls.MISSED_TYPE -> CallKind.MISSED
        CallLog.Calls.REJECTED_TYPE -> CallKind.REJECTED
        CallLog.Calls.BLOCKED_TYPE -> CallKind.BLOCKED
        CallLog.Calls.VOICEMAIL_TYPE -> CallKind.VOICEMAIL
        else -> CallKind.UNKNOWN
    }

    private class ContactBuilder(
        val id: Long,
        val lookupKey: String,
        val name: String,
        val photoUri: String?,
        val starred: Boolean,
        val sortKey: String,
        val organization: String?,
    ) {
        private val numbers = LinkedHashMap<String, ContactNumber>()

        fun add(number: ContactNumber) {
            val key = number.normalized.ifEmpty { number.number }
            numbers.putIfAbsent(key, number)
        }

        fun build(): Contact = Contact(
            id = id,
            lookupKey = lookupKey,
            name = name,
            photoUri = photoUri,
            numbers = numbers.values.toList(),
            starred = starred,
            sortKey = sortKey,
            organization = organization,
        )
    }

    companion object {
        const val NONE_SOURCE = "__none__"
    }
}
