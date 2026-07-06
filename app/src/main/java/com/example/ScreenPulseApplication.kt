package com.example

import android.app.Application
import com.example.data.database.ScreenPulseDatabase
import com.example.data.datastore.SettingsManager
import com.example.data.repository.UsageRepository

class ScreenPulseApplication : Application() {

    lateinit var repository: UsageRepository
        private set

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()

        val database = ScreenPulseDatabase.getDatabase(this)
        settingsManager = SettingsManager(this)
        repository = UsageRepository(this, database.usageDao(), settingsManager)
    }
}
