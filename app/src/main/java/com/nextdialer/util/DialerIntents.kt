package com.nextdialer.util

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat

object DialerIntents {

    private fun telUri(number: String): Uri = Uri.fromParts("tel", number, null)

    private fun start(context: Context, intent: Intent) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun call(context: Context, number: String) {
        if (number.isBlank()) return
        val canCall = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE,
        ) == PackageManager.PERMISSION_GRANTED
        val action = if (canCall) Intent.ACTION_CALL else Intent.ACTION_DIAL
        try {
            start(context, Intent(action, telUri(number)))
        } catch (_: SecurityException) {
            dial(context, number)
        } catch (_: ActivityNotFoundException) {
            dial(context, number)
        }
    }

    fun dial(context: Context, number: String) {
        if (number.isBlank()) return
        runCatching { start(context, Intent(Intent.ACTION_DIAL, telUri(number))) }
    }

    fun sms(context: Context, number: String) {
        if (number.isBlank()) return
        runCatching {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null))
                .putExtra("sms_body", "")
            start(context, intent)
        }
    }

    fun email(context: Context, address: String) {
        if (address.isBlank()) return
        runCatching {
            start(context, Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)))
        }
    }

    fun editContact(context: Context, contactId: Long) {
        runCatching {
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_URI,
                contactId.toString(),
            )
            start(context, Intent(Intent.ACTION_EDIT, uri))
        }
    }

    fun insertContact(context: Context, number: String? = null) {
        runCatching {
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
            if (!number.isNullOrBlank()) {
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number)
            }
            start(context, intent)
        }
    }

    fun shareContact(context: Context, name: String, number: String?) {
        val text = buildString {
            append(name)
            if (!number.isNullOrBlank()) append("\n").append(number)
        }
        runCatching {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            start(context, Intent.createChooser(send, "Share contact"))
        }
    }

    fun openContactExternally(context: Context, contactId: Long) {
        runCatching {
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_URI,
                contactId.toString(),
            )
            val view = Intent(Intent.ACTION_VIEW, uri)
            start(context, Intent.createChooser(view, "Open contact with"))
        }
    }

    /**
     * The original "VoIP call" action let the user pick a third-party handler. Android has no
     * universal VoIP intent, so this offers every app that can handle a tel: dial intent, minus
     * NEXT Dialer itself.
     */
    fun voip(context: Context, number: String) {
        if (number.isBlank()) return
        val uri = telUri(number)
        val probe = Intent(Intent.ACTION_DIAL, uri)
        val targets = runCatching {
            context.packageManager.queryIntentActivities(probe, 0)
                .filter { it.activityInfo.packageName != context.packageName }
        }.getOrDefault(emptyList())

        if (targets.isEmpty()) {
            call(context, number)
            return
        }

        runCatching {
            val first = targets.first().activityInfo.packageName
            val base = Intent(Intent.ACTION_DIAL, uri).setPackage(first)
            val chooser = Intent.createChooser(base, "Call with")
            if (targets.size > 1) {
                val extras = targets.drop(1)
                    .map { Intent(Intent.ACTION_DIAL, uri).setPackage(it.activityInfo.packageName) }
                    .toTypedArray()
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extras)
            }
            start(context, chooser)
        }
    }

    fun openAppSettings(context: Context) {
        runCatching {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            )
            start(context, intent)
        }
    }

    private fun isInstalled(context: Context, packageName: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)

    fun whatsAppPackages(context: Context): List<String> =
        listOf("com.whatsapp", "com.whatsapp.w4b").filter { isInstalled(context, it) }

    fun telegramInstalled(context: Context): Boolean =
        isInstalled(context, "org.telegram.messenger") ||
            isInstalled(context, "org.telegram.plus")

    /** WhatsApp has no public calling intent, so the deep link opens the chat with the number. */
    fun whatsAppChat(context: Context, number: String, packageName: String) {
        val digits = PhoneUtils.normalize(number)
        if (digits.isEmpty()) return
        runCatching {
            val uri = Uri.parse("https://wa.me/$digits")
            start(
                context,
                Intent(Intent.ACTION_VIEW, uri).setPackage(packageName),
            )
        }.onFailure {
            runCatching { start(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))) }
        }
    }

    fun telegramChat(context: Context, number: String) {
        val digits = PhoneUtils.normalize(number)
        if (digits.isEmpty()) return
        runCatching {
            start(
                context,
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?phone=$digits"))
                    .setPackage("org.telegram.messenger"),
            )
        }.onFailure {
            runCatching { start(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+$digits"))) }
        }
    }
}
