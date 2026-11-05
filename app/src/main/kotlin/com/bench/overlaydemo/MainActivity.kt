package com.bench.overlaydemo

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * Launcher activity.
 *
 * Flow:
 *  1. Check SYSTEM_ALERT_WINDOW permission — if missing, open the system
 *     "Draw over other apps" settings page and wait for the user to grant it.
 *  2. On API 33+ also request POST_NOTIFICATIONS so the foreground service
 *     notification shows up.
 *  3. Start / stop OverlayService via the two buttons.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    // ── Permission launchers ──────────────────────────────────────────────────
    private val overlayPermLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshUi() }

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshUi() }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop  = findViewById(R.id.btnStop)

        btnStart.setOnClickListener { handleStart() }
        btnStop.setOnClickListener  { stopOverlay() }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    // ── Permission helpers ────────────────────────────────────────────────────
    private fun canDrawOverlay(): Boolean =
        Settings.canDrawOverlays(this)

    private fun hasNotifPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return getSystemService(NotificationManager::class.java)
            ?.areNotificationsEnabled() == true
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermLauncher.launch(intent)
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    private fun refreshUi() {
        val overlayOk = canDrawOverlay()
        val notifOk   = hasNotifPermission()

        tvStatus.text = when {
            !overlayOk -> "⚠ Draw-over-apps permission required"
            !notifOk   -> "⚠ Notification permission required (optional)"
            else       -> "✓ Ready — tap Start Overlay"
        }

        btnStart.isEnabled = overlayOk
        btnStop.isEnabled  = overlayOk
    }

    private fun handleStart() {
        when {
            !canDrawOverlay()   -> requestOverlayPermission()
            !hasNotifPermission() -> requestNotifPermission()
            else                -> startOverlay()
        }
    }

    // ── Service control ───────────────────────────────────────────────────────
    private fun startOverlay() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        tvStatus.text = "Overlay running — go to home screen to see it"
    }

    private fun stopOverlay() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOP
        }
        startService(intent)
        tvStatus.text = "Overlay stopped"
    }
}
