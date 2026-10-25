package com.nextdialer.data

data class ContactNumber(
    val number: String,
    val normalized: String,
    val type: Int,
    val label: String?,
    val typeLabel: String,
)

data class Contact(
    val id: Long,
    val lookupKey: String,
    val name: String,
    val photoUri: String?,
    val numbers: List<ContactNumber>,
    val starred: Boolean,
    val sortKey: String,
) {
    val primaryNumber: String? get() = numbers.firstOrNull()?.number
    val initial: String
        get() = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
}

data class ContactDetail(
    val contact: Contact,
    val emails: List<String>,
)

enum class CallKind {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    VOICEMAIL,
    UNKNOWN,
}

data class CallEntry(
    val id: Long,
    val number: String,
    val displayName: String?,
    val photoUri: String?,
    val date: Long,
    val duration: Long,
    val kind: CallKind,
    val isNew: Boolean,
    val contactId: Long?,
    val numberLabel: String?,
    /** Number of call-log rows merged into this row. */
    val count: Int = 1,
    /** Every call-log id behind this row, newest first. Used for deletion. */
    val ids: List<Long> = listOf(id),
    /** Ids of missed calls that are still marked new in the call log. */
    val unreadIds: List<Long> = if (kind == CallKind.MISSED && isNew) listOf(id) else emptyList(),
)

enum class SwipeAction {
    CALL,
    SMS,
    VIEW,
    VOIP,
    /** Deletes every call-log row for the number (needs WRITE_CALL_LOG). */
    DELETE,
    NONE,
}

enum class TapAction {
    VIEW,
    CALL,
    SMS,
    VOIP,
}
