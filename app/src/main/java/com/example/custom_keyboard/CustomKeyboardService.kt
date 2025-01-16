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
    private var keySound: MediaPlayer? = null
    private var isSpecial = false

    private var currentLanguageIndex = 0
    private val languageLayouts = mapOf(
        "English" to R.xml.keyboard_layout,
        "Russian" to R.xml.russian_layout
    )
    private val languages = languageLayouts.keys.toList()



    override fun onCreate() {
        super.onCreate()
        try {
            keySound = MediaPlayer.create(this, R.raw.typewriter)
        } catch (e: Exception) {
            e.printStackTrace()
            keySound = null
        }
    }

    override fun onCreateInputView(): KeyboardView {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView

        // loadLanguagePreference()

        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    private fun loadLanguagePreference() {
        val sharedPreferences = getSharedPreferences("keyboard_prefs", MODE_PRIVATE)
        val selectedLanguage = sharedPreferences.getString("selected_language", "English")
        println("selected Lang=> $selectedLanguage")
        currentLanguageIndex = languages.indexOf(selectedLanguage)
        if (currentLanguageIndex == -1) currentLanguageIndex = 0 // Fallback if saved language is invalid
    }


    private fun switchLanguage() {
        currentLanguageIndex = (currentLanguageIndex + 1) % languages.size
        val selectedLanguage = languages[currentLanguageIndex]

        val layoutResId = languageLayouts[selectedLanguage] ?: R.xml.keyboard_layout

        keyboard = Keyboard(this, layoutResId)
        keyboardView.keyboard = keyboard
        keyboardView.invalidateAllKeys()

        // saveLanguagePreference()
    }

    private fun saveLanguagePreference() {
        val sharedPreferences = getSharedPreferences("keyboard_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selected_language", languages[currentLanguageIndex])
        editor.apply()
        println("CustomKeyboardService=> ${languages[currentLanguageIndex]}")
    }


//    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
//        super.onStartInputView(info, restarting)
//        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
//        val params = window?.window?.attributes
//        if (isPortrait) {
//            val screenHeight = resources.displayMetrics.heightPixels
//            val keyboardHeight = (screenHeight * 0.4).toInt() // 40% of the screen height
//            params?.height = keyboardHeight
//        }
//        window?.window?.attributes = params
//    }

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection
        keySound?.let {
            it.stop()
            it.prepare()
            it.start()
        }

        when (primaryCode) {
            -10 -> switchLanguage()

            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_SHIFT -> {
                keyboard.isShifted = !keyboard.isShifted
                keyboardView.invalidateAllKeys()
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

