package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                val serviceIntent = Intent(context, ScreenPulseService::class.java)
                context.startService(serviceIntent)
            } catch (e: Exception) {
                // Ignore background start restrictions on Android 8+ if not in foreground, but sticky service handles restarts
            }
        }
    }
}
