@file:Suppress("DEPRECATION")

package com.example.custom_keyboard
import android.content.res.Configuration
import android.graphics.Canvas
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.media.MediaPlayer
import android.view.inputmethod.EditorInfo

class CustomKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard
    private var keySound: MediaPlayer? = null
    private var isSpecial = false

    override fun onCreate() {
        super.onCreate()
        try {
            keySound = MediaPlayer.create(this, R.raw.typewriter)
        } catch (e: Exception) {
            e.printStackTrace()
            keySound = null
        }
    }

//    override fun onCreateInputView(): KeyboardView {
//        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
//        keyboard = Keyboard(this, R.xml.keyboard_layout)
//        keyboardView.keyboard = keyboard
//        keyboardView.isPreviewEnabled = false
//        keyboardView.setOnKeyboardActionListener(this)
//
//        return keyboardView
//    }

//    override fun onCreateInputView(): KeyboardView {
//        val keyboardView = CustomKeyboardView(this, null)
//        keyboard = Keyboard(this, R.xml.keyboard_layout)
//        keyboardView.keyboard = keyboard
//        keyboardView.isPreviewEnabled = false
//        keyboardView.setOnKeyboardActionListener(this)
//        return keyboardView
//    }

    override fun onCreateInputView(): KeyboardView {
        keyboard = Keyboard(this, R.xml.keyboard_layout) // Initialize keyboard first
        val keyboardView = CustomKeyboardView(this, null)
        keyboardView.setCustomKeyboard(keyboard) // Pass the keyboard explicitly after initialization
        keyboardView.keyboard = keyboard // Set the keyboard explicitly to avoid nulls
        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)
        this.keyboardView = keyboardView // Properly initialize the lateinit property
        return keyboardView
    }



    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection
        keySound?.let {
            it.stop()
            it.prepare()
            it.start()
        }

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_SHIFT -> {
                keyboard.isShifted = !keyboard.isShifted
                (keyboardView as? CustomKeyboardView)?.updateShiftState(keyboard.isShifted)
                // keyboardView.invalidateAllKeys()
            }

            Keyboard.KEYCODE_MODE_CHANGE -> {
                if (isSpecial) {
                    keyboard = Keyboard(this, R.xml.keyboard_layout)
                    keyboardView.keyboard = keyboard
                    isSpecial = false
                } else {
                    keyboard = Keyboard(this, R.xml.keyboard_special_chars)
                    keyboardView.keyboard = keyboard
                    isSpecial = true
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
                if (Character.isLetter(code) && keyboard.isShifted) {
                    code = Character.toUpperCase(code)
                    keyboard.isShifted = !keyboard.isShifted
                    keyboardView.invalidateAllKeys()
                } else {
                    code = Character.toLowerCase(code)
                }
                ic.commitText(code.toString(), 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keySound?.release()
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

// TODO maybe to default to Android keycodes instead of ASCII

//   TODO
//    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
//        if (currentInputConnection != null) {
//            keySound?.let {
//                it.stop()
//                it.prepare()
//                it.start()
//            }
//            when (primaryCode) {
//                Keyboard.KEYCODE_MODE_CHANGE -> enableSpecialCharKeyboard()
//                44 -> enableAlphaKeyboard()
//                Keyboard.KEYCODE_DELETE -> handleDelete()
//                Keyboard.KEYCODE_SHIFT -> handleShift()
//                else -> encodeCharacter(primaryCode)
//            }
//        }
//    }

