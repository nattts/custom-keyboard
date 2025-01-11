@file:Suppress("DEPRECATION")

package com.example.custom_keyboard
import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.Keyboard
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

class CustomKeyboardView(context: Context, attrs: AttributeSet?) :
    KeyboardView(context, attrs) {

    private var customKeyboard: Keyboard? = null
    private var isShifted: Boolean = false

    fun setCustomKeyboard(keyboard: Keyboard) {
        this.customKeyboard = keyboard
    }

    fun updateShiftState(isShifted: Boolean) {
        this.isShifted = isShifted
        invalidate() // Redraw the keyboard to reflect the shift state
    }

    @SuppressLint("DrawAllocation")
    @Deprecated("Deprecated in Java")
    override fun onDraw(canvas: Canvas) {
        if (customKeyboard == null) {
            // Log if the keyboard is null for debugging
            android.util.Log.e("CustomKeyboardView", "Keyboard is null in onDraw")
            return // Extra safety check
        }
        super.onDraw(canvas)

        val paint = Paint().apply {
            textSize = 32f // Adjust text size
            color = Color.WHITE // Set text color
            textAlign = Paint.Align.CENTER
        }

        customKeyboard?.keys?.forEach { key ->
            // Draw the icon if available
            key.icon?.let { icon ->
                icon.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
                icon.draw(canvas)
            }

            // Draw the label on top of the icon, considering shift state
            key.label?.let { label ->
                val displayedLabel = if (isShifted) label.toString().uppercase() else label.toString()
                canvas.drawText(
                    displayedLabel,
                    (key.x + key.width / 2).toFloat(),
                    (key.y + key.height / 2).toFloat() + paint.textSize / 2,
                    paint
                )
            }
        }
    }
}
