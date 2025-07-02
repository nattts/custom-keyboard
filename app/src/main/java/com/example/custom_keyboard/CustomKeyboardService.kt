@file:Suppress("DEPRECATION")
package com.example.custom_keyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import java.io.PrintWriter
import java.io.StringWriter
import com.example.custom_keyboard.utils.logMessage
import com.example.custom_keyboard.utils.getStackTrace
import com.example.custom_keyboard.utils.initializeLogFile

class CustomKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard
    private var isSpecial = false

    private var keySound: MediaPlayer? = null
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    private var currentLanguageIndex = 0
    private val languageLayouts = mapOf(
        "English" to R.xml.keyboard_layout,
        "Russian" to R.xml.russian_layout,
        "Latvian" to R.xml.latvian_layout
    )
    private val languages = languageLayouts.keys.toList()
    private val logFileUri: Uri? = null
    private var currentLang: String? = "English"
    private var layoutResId: Int = 0


    override fun onCreate() {
        super.onCreate()

        // Set up a global exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val writer = StringWriter()
            throwable.printStackTrace(PrintWriter(writer))
            val stackTrace = writer.toString()
            logMessage(this, logFileUri, "STACK TRACE:\n$stackTrace\n")
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Number of simultaneous sounds
            .build()
        try {
            soundId = soundPool.load(this, R.raw.keyboard_tactile_3, 1)
            // soundId = soundPool.load(this, R.raw.keyboard_tactile_14, 1)
            // soundId = soundPool.load(this, R.raw.keyboard_tactile_15, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            keySound = null
        }
    }


    override fun onCreateInputView(): KeyboardView {
        initializeLogFile(this, logFileUri)
        println("onCreateInputView => ")

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        val spaceKey = keyboard.keys.firstOrNull { it.codes.contains(62) }
        spaceKey?.label = currentLang

        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    private fun switchLanguage() {
        try {
            currentLanguageIndex = (currentLanguageIndex + 1) % languages.size
            val selectedLanguage = languages[currentLanguageIndex]

            layoutResId = languageLayouts[selectedLanguage] ?: R.xml.keyboard_layout
            keyboard = Keyboard(this, layoutResId)
            keyboardView.keyboard = keyboard

            val spaceKey = keyboard.keys.firstOrNull { it.codes.contains(62) }
            spaceKey?.label = selectedLanguage
            currentLang = selectedLanguage

            keyboardView.invalidateAllKeys()

        } catch (error: Exception) {
            logMessage(this, logFileUri, "Error in switchLanguage: ${error.message}")
            logMessage(this, logFileUri, "Error switchLanguage stacktrace: ${getStackTrace(error)}")
        }
    }

    private fun setCurrentLayout(currentLang: String?) {
        layoutResId = languageLayouts[currentLang] ?: R.xml.keyboard_layout
        keyboard = Keyboard(this, layoutResId)
        keyboardView.keyboard = keyboard

            val spaceKey = keyboard.keys.firstOrNull { it.codes.contains(62) }
            spaceKey?.label = currentLang

        keyboardView.invalidateAllKeys()
    }

    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        try {
            val ic: InputConnection = currentInputConnection
            soundPool.play(
                soundId,
                1f, // Left volume
                1f, // Right volume
                0,  // Priority
                0,  // Loop (0 = no loop)
                1f  // Playback speed
            )

            when (primaryCode) {
                -10 -> switchLanguage()

                Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
                Keyboard.KEYCODE_SHIFT -> {
                    keyboard.isShifted = !keyboard.isShifted
                    keyboardView.invalidateAllKeys()
                }


                Keyboard.KEYCODE_MODE_CHANGE -> {
                    if (isSpecial) {
                        isSpecial = false
                        setCurrentLayout(currentLang)
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
        } catch (error: Exception) {
            logMessage(this, logFileUri, "Error in onKey: ${error.message}")
            logMessage(this, logFileUri, "Error in onKey stacktrace: ${getStackTrace(error)}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
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

