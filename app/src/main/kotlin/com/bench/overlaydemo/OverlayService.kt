package com.bench.overlaydemo

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlin.math.sin

/**
 * Foreground service that owns the overlay window and animates demo entities.
 * No game-memory, no ptrace, no process injection — purely a UI demo.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.bench.overlaydemo.START"
        const val ACTION_STOP  = "com.bench.overlaydemo.STOP"
        private const val NOTIF_CHANNEL = "overlay_demo_channel"
        private const val NOTIF_ID      = 1001
    }

    private lateinit var wm: WindowManager
    private lateinit var overlay: OverlaySurfaceView
    private var animThread: Thread? = null
    private var screenW = 1080f
    private var screenH = 1920f

    // ── Service lifecycle ─────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        // Grab display size
        val metrics = resources.displayMetrics
        screenW = metrics.widthPixels.toFloat()
        screenH = metrics.heightPixels.toFloat()

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        setupOverlay()
        startAnimation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        animThread?.interrupt()
        overlay.stopRendering()
        try { wm.removeView(overlay) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Overlay window ────────────────────────────────────────────────────────
    private fun setupOverlay() {
        overlay = OverlaySurfaceView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        wm.addView(overlay, params)
        overlay.startRendering()
    }

    // ── Demo animation — no game data, purely synthetic ───────────────────────
    private fun startAnimation() {
        animThread = Thread {
            var t = 0.0
            val labels = listOf("Alpha", "Bravo", "Charlie", "Delta", "Echo")

            while (!Thread.currentThread().isInterrupted) {
                t += 0.025

                // Five entities orbiting the screen at different radii/speeds
                val updated = List(5) { i ->
                    val angle   = t * (0.7 + i * 0.15) + i * Math.PI * 0.4
                    val radiusX = screenW * (0.22 + i * 0.04)
                    val radiusY = screenH * (0.18 + i * 0.03)
                    val cx      = screenW / 2f + (radiusX * sin(angle)).toFloat()
                    val cy      = screenH / 2f + (radiusY * sin(angle * 0.6 + i)).toFloat()
                    val boxH    = screenH * (0.14f + i * 0.01f)
                    val hp      = ((sin(t * 0.4 + i) * 0.5 + 0.5) * 100).toInt().coerceIn(1, 100)

                    DemoEntity(
                        screenX     = cx,
                        screenY     = cy - boxH / 2f,
                        screenFeetY = cy + boxH / 2f,
                        health      = hp,
                        label       = labels[i],
                        isEnemy     = i % 2 == 0
                    )
                }

                overlay.entities = updated
                Thread.sleep(16L)
            }
        }.also { it.isDaemon = true; it.start() }
    }

    // ── Notification ──────────────────────────────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                NOTIF_CHANNEL,
                "Overlay Demo",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Overlay demo service" }
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setContentTitle("Overlay Demo")
            .setContentText("Overlay is active — tap Stop to dismiss")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .build()
    }
}
