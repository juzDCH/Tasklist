package com.example.tasklist.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progress = 0

    private val strokeWidth = 12f
    private val radiusOffset = strokeWidth / 2

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0") // Gris claro de fondo
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressView.strokeWidth
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BCD4") // Celeste del logo
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = this@CircularProgressView.strokeWidth
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = width.coerceAtMost(height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f - radiusOffset

        val rect = RectF(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        // Fondo gris
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Progreso en celeste
        canvas.drawArc(rect, -90f, 360f * progress / 100f, false, progressPaint)

        // Texto centrado
        val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText("$progress%", cx, baseline, textPaint)
    }
}
