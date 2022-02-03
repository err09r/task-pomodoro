package com.app.pomodoro.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.app.pomodoro.R

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var period = 0L
    private var currentSec = 0L

    private val paint = Paint().apply {
        strokeWidth = STROKE_WIDTH
    }

    private var color = 0
    private var style = FILL

    init {
        if (attrs != null) {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.CustomView)
            color = styledAttrs.getColor(R.styleable.CustomView_custom_color, Color.RED)
            style = styledAttrs.getInt(R.styleable.CustomView_custom_style, FILL)
            styledAttrs.recycle()
        }
        paint.color = color
        paint.style = if (style == FILL) Paint.Style.FILL else Paint.Style.STROKE

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (period == 0L || currentSec == 0L) return

        val sweepAngle = (((currentSec % period).toFloat() / period) * 360)
        canvas.drawArc(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            270f,
            sweepAngle,
            true,
            paint
        )

    }

    fun setCurrent(currentSec: Long) {
        this.currentSec = currentSec
        invalidate()
    }

    fun setPeriod(period: Long) {
        this.period = period
    }

    private companion object {

        private const val STROKE_WIDTH = 5f
        private const val FILL = 0
    }
}