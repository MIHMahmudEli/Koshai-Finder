package com.example.koshailagbe.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DimOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#99000000") // 60% black dim
        style = Paint.Style.FILL
    }
    
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        color = Color.TRANSPARENT
    }

    var holeRect: RectF? = null
        set(value) {
            field = value
            invalidate()
        }
        
    var holeRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        // LAYER_TYPE_HARDWARE supports Mode.CLEAR if set properly, but SOFTWARE is a safe fallback if issues arise.
        // For modern Android, HARDWARE usually supports CLEAR. Let's stick to hardware to avoid performance hits.
    }

    override fun onDraw(canvas: Canvas) {
        // Save the layer to ensure PorterDuff.Mode.CLEAR only clears the dim layer, not the entire window background
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        holeRect?.let {
            canvas.drawRoundRect(it, holeRadius, holeRadius, clearPaint)
        }
        
        canvas.restoreToCount(saveCount)
    }
}
