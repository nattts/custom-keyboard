@file:Suppress("DEPRECATION")
package com.example.custom_keyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.media.MediaPlayer

class CustomKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard
    private lateinit var keySound: MediaPlayer

    private var isCaps = false
    private var isSpecial = false

    override fun onCreateInputView(): KeyboardView {
        // Inflate the keyboard view
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        // Load custom sound
        keySound = MediaPlayer.create(this, R.raw.typewriter)

        return keyboardView
    }


    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection
        keySound.start()
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_SHIFT -> {
                isCaps = !isCaps
                keyboard.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }

            44 -> {
                if (!isSpecial) {
                    keyboardView.keyboard = Keyboard(this, R.xml.keyboard_layout)
                    isSpecial = !isSpecial
                } else {
                    keyboardView.keyboard = Keyboard(this, R.xml.keyboard_special_chars)
                    isSpecial = !isSpecial
                }

            }

            KeyEvent.KEYCODE_SPACE -> {
                currentInputConnection?.commitText(" ", 1)
            }

            Keyboard.KEYCODE_DONE -> ic.sendKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_ENTER
                )
            )

            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && isCaps) {
                    code = Character.toUpperCase(code)
                }
                ic.commitText(code.toString(), 1)
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onText(text: CharSequence?) {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun swipeLeft() {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun swipeRight() {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun swipeDown() {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun swipeUp() {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onPress(primaryCode: Int) {}

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onRelease(primaryCode: Int) {}
}
