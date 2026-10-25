package com.nextdialer.util

import com.nextdialer.data.Contact
import java.text.Normalizer

object PhoneUtils {

    private val t9Map: Map<Char, Char> = buildMap {
        fun put(digit: Char, letters: String) = letters.forEach { put(it, digit) }
        put('2', "abc")
        put('3', "def")
        put('4', "ghi")
        put('5', "jkl")
        put('6', "mno")
        put('7', "pqrs")
        put('8', "tuv")
        put('9', "wxyz")
    }

    fun digits(value: String): String = value.filter { it.isDigit() }

    fun normalize(value: String): String = digits(value)

    fun looksLikePhone(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.length !in 5..25) return false
        if (trimmed.any { it.isLetter() }) return false
        if (trimmed.any { it !in "0123456789+- ()./" }) return false
        val count = trimmed.count { it.isDigit() }
        return count in 5..15
    }

    fun numbersMatch(first: String, second: String): Boolean {
        val a = digits(first)
        val b = digits(second)
        if (a.isEmpty() || b.isEmpty()) return false
        if (a == b) return true
        if (a.length >= 7 && b.length >= 7) {
            return a.takeLast(7) == b.takeLast(7)
        }
        return false
    }

    fun t9ForName(name: String): String {
        val stripped = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
        return buildString {
            for (ch in stripped) {
                when {
                    ch.isDigit() -> append(ch)
                    else -> t9Map[ch]?.let { append(it) }
                }
            }
        }
    }

    fun initialLetter(name: String): String {
        val first = Normalizer.normalize(name.trim().take(1), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .firstOrNull()
            ?.uppercaseChar()
        return if (first != null && first in 'A'..'Z') first.toString() else "#"
    }

    fun matchesContact(contact: Contact, query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        val digits = digits(trimmed)
        val lowered = trimmed.lowercase()

        if (contact.name.lowercase().contains(lowered)) return true
        if (digits.isNotEmpty() && t9ForName(contact.name).contains(digits)) return true
        return contact.numbers.any { number ->
            number.number.contains(trimmed, ignoreCase = true) ||
                (digits.isNotEmpty() && number.normalized.contains(digits))
        }
    }
}
