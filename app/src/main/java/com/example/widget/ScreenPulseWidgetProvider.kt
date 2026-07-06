package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.ScreenPulseApplication
import com.example.data.database.ScreenPulseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

open class ScreenPulseWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as ScreenPulseApplication
        val database = ScreenPulseDatabase.getDatabase(context)
        val dao = database.usageDao()

        scope.launch {
            try {
                // Fetch dynamic counts from Database
                val contacts = dao.getAllContacts().first()
                val callLogs = dao.getAllCallLogs().first()

                appWidgetIds.forEach { widgetId ->
                    val views = resolveWidgetView(
                        context = context,
                        widgetId = widgetId,
                        appWidgetManager = appWidgetManager,
                        contactsCount = contacts.size,
                        callsCount = callLogs.size
                    )

                    // Add PendingIntent to open App on click
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } catch (e: Exception) {
                // Prevent widget crashing
            }
        }
    }

    private fun resolveWidgetView(
        context: Context,
        widgetId: Int,
        appWidgetManager: AppWidgetManager,
        contactsCount: Int,
        callsCount: Int
    ): RemoteViews {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val contactsStr = "$contactsCount Kişi"
        val callsStr = "$callsCount Arama"
        val headerStr = "Rehber & Aramalar"

        return when {
            minWidth >= 200 && minHeight >= 200 -> {
                // 4x4 Widget
                RemoteViews(context.packageName, R.layout.widget_4x4).apply {
                    setTextViewText(R.id.widget_sot_value, contactsStr)
                    setTextViewText(R.id.widget_screen_off_value, callsStr)
                    setTextViewText(R.id.widget_last_charge_time, headerStr)
                    setTextViewText(R.id.widget_temp_value, "Aktif")
                    setTextViewText(R.id.widget_voltage_value, "Rehber")
                    
                    // Draw circular visualization
                    val bitmap = drawCircularIndicator(contactsCount)
                    setImageViewBitmap(R.id.widget_battery_circle, bitmap)
                }
            }
            minWidth >= 200 -> {
                // 4x2 Widget
                RemoteViews(context.packageName, R.layout.widget_4x2).apply {
                    setTextViewText(R.id.widget_sot_value, contactsStr)
                    setTextViewText(R.id.widget_last_charge_time, callsStr)
                    
                    // Draw circular visualization
                    val bitmap = drawCircularIndicator(contactsCount)
                    setImageViewBitmap(R.id.widget_battery_circle, bitmap)
                }
            }
            else -> {
                // 2x2 Widget
                RemoteViews(context.packageName, R.layout.widget_2x2).apply {
                    setTextViewText(R.id.widget_sot_value, contactsStr)
                    setTextViewText(R.id.widget_battery, callsStr)
                }
            }
        }
    }

    private fun drawCircularIndicator(contactsCount: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintTrack = Paint().apply {
            color = Color.parseColor("#15FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 14f
            isAntiAlias = true
        }

        val paintProgress = Paint().apply {
            color = Color.parseColor("#4CAF50") // Green indicator
            style = Paint.Style.STROKE
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val paintLabel = Paint().apply {
            color = Color.parseColor("#88FFFFFF")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val rect = RectF(16f, 16f, 144f, 144f)
        canvas.drawArc(rect, 0f, 360f, false, paintTrack)
        
        // Progress sweep based on contact count (clamped to max 100 for visual)
        val percentage = (contactsCount * 5).coerceIn(10, 100)
        canvas.drawArc(rect, -90f, percentage * 3.6f, false, paintProgress)

        canvas.drawText("$contactsCount", 80f, 85f, paintText)
        canvas.drawText("KİŞİ", 80f, 112f, paintLabel)

        return bitmap
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        job.cancel()
    }
}

class ScreenPulseWidgetProvider2x2 : ScreenPulseWidgetProvider()
class ScreenPulseWidgetProvider4x2 : ScreenPulseWidgetProvider()
class ScreenPulseWidgetProvider4x4 : ScreenPulseWidgetProvider()
