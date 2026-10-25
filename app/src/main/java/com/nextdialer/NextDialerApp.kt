package com.nextdialer

import android.app.Application
import com.nextdialer.data.SettingsStore

class NextDialerApp : Application() {
    val settings: SettingsStore by lazy { SettingsStore(this) }
}
