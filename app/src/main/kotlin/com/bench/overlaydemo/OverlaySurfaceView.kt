package com.bench.overlaydemo

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * SurfaceView that renders configurable demo entities as bounding boxes + health bars.
 * No game-memory reading, no process access — purely a Canvas drawing demo.
 */
class OverlaySurfaceView(ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback {

    // ── Public entity feed ────────────────────────────────────────────────────
    @Volatile
    var entities: List<DemoEntity> = emptyList()

    // ── Render thread ─────────────────────────────────────────────────────────
    private val running = AtomicBoolean(false)
    private var renderThread: Thread? = null

    // ── Paints ────────────────────────────────────────────────────────────────
    private val paintEnemy = Paint().apply {
        color = Color.argb(230, 255, 60, 60)
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        isAntiAlias = true
    }
    private val paintTeam = Paint().apply {
        color = Color.argb(230, 60, 220, 80)
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        isAntiAlias = true
    }
    private val healthBg = Paint().apply {
        color = Color.argb(180, 20, 20, 20)
        style = Paint.Style.FILL
    }
    private val healthFill = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 26f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        setShadowLayer(4f, 1f, 1f, Color.BLACK)
    }
    private val hpTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 22f
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    init {
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(this)
    }

    fun startRendering() {
        running.set(true)
        renderThread = Thread {
            while (running.get()) {
                val canvas = holder.lockCanvas() ?: continue
                try {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    drawEntities(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(16L) // ~60 fps
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun stopRendering() {
        running.set(false)
        renderThread?.join(500)
        renderThread = null
    }

    // ── Drawing ───────────────────────────────────────────────────────────────
    private fun drawEntities(c: Canvas) {
        val snap = entities
        for (ent in snap) {
            val boxH = ent.screenFeetY - ent.screenY
            if (boxH < 8f) continue

            val boxW  = boxH * 0.42f
            val cx    = ent.screenX
            val left  = cx - boxW / 2f
            val right = cx + boxW / 2f
            val top   = ent.screenY
            val bot   = ent.screenFeetY

            val stroke = if (ent.isEnemy) paintEnemy else paintTeam

            // ── Bounding box ──────────────────────────────────────────────
            c.drawRect(left, top, right, bot, stroke)

            // ── Corner ticks ──────────────────────────────────────────────
            val tk = boxW * 0.22f
            with(c) {
                drawLine(left,       top,      left + tk,  top,      stroke)
                drawLine(left,       top,      left,       top + tk, stroke)
                drawLine(right - tk, top,      right,      top,      stroke)
                drawLine(right,      top,      right,      top + tk, stroke)
                drawLine(left,       bot - tk, left,       bot,      stroke)
                drawLine(left,       bot,      left + tk,  bot,      stroke)
                drawLine(right - tk, bot,      right,      bot,      stroke)
                drawLine(right,      bot - tk, right,      bot,      stroke)
            }

            // ── Health bar (left of box) ───────────────────────────────────
            val barW    = 7f
            val barGap  = 4f
            val barL    = left - barW - barGap
            val barH    = bot - top
            val fillH   = barH * (min(100, max(0, ent.health)) / 100f)

            c.drawRect(barL, top, barL + barW, bot, healthBg)

            // gradient: green → yellow → red based on HP
            val hp = ent.health.coerceIn(0, 100)
            healthFill.color = when {
                hp > 60 -> Color.argb(220, 60, 210, 70)
                hp > 30 -> Color.argb(220, 230, 200, 40)
                else    -> Color.argb(220, 220, 60, 60)
            }
            c.drawRect(barL, bot - fillH, barL + barW, bot, healthFill)

            // HP number below bar
            val hpStr = "$hp"
            c.drawText(hpStr, barL - 1f, bot + 20f, hpTextPaint)

            // ── Label below box ───────────────────────────────────────────
            if (ent.label.isNotEmpty()) {
                val tw = textPaint.measureText(ent.label)
                c.drawText(ent.label, cx - tw / 2f, bot + 36f, textPaint)
            }
        }
    }

    // ── SurfaceHolder callbacks ───────────────────────────────────────────────
    override fun surfaceCreated(h: SurfaceHolder) {}
    override fun surfaceChanged(h: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(h: SurfaceHolder) { stopRendering() }
}
