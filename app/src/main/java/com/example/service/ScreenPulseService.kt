package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.example.ScreenPulseApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScreenPulseService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        registerScreenPulseReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenPulseReceiver() {
        if (screenReceiver != null) return

        val app = applicationContext as ScreenPulseApplication
        val settingsManager = app.settingsManager

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action ?: return
                serviceScope.launch {
                    val now = System.currentTimeMillis()
                    when (action) {
                        Intent.ACTION_SCREEN_OFF -> {
                            settingsManager.saveScreenOffStartTime(now)
                        }
                        Intent.ACTION_SCREEN_ON -> {
                            val startTime = settingsManager.screenOffStartTime.first()
                            if (startTime > 0L) {
                                val duration = now - startTime
                                if (duration > 0 && duration < 12 * 3600 * 1000L) { // max 12 hours safety
                                    settingsManager.accumulateScreenOffTime(duration)
                                }
                            }
                        }
                        Intent.ACTION_USER_PRESENT -> {
                            settingsManager.incrementUnlockCount()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {}
        }
    }
}
