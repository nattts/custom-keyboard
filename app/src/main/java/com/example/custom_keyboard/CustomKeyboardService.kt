@file:Suppress("DEPRECATION")
package com.example.custom_keyboard

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

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
        "Russian" to R.xml.russian_layout
    )
    private val languages = languageLayouts.keys.toList()

    private lateinit var logFile: File
    private var logFileUri: Uri? = null


    override fun onCreate() {
        super.onCreate()

        // Set up a global exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val writer = StringWriter()
            throwable.printStackTrace(PrintWriter(writer))
            val stackTrace = writer.toString()
            logMessage("STACK TRACE:\n$stackTrace\n")
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Number of simultaneous sounds
            .build()
        try {
            // soundId = soundPool.load(this, R.raw.typewriter, 1)
            // soundId = soundPool.load(this, R.raw.keyboard_tactile_3, 1)
            // soundId = soundPool.load(this, R.raw.keyboard_tactile_14, 1)
            soundId = soundPool.load(this, R.raw.keyboard_tactile_15, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            keySound = null
        }
    }


    override fun onCreateInputView(): KeyboardView {
        // initializeLogFile()

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView

        // loadLanguagePreference()

        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

//    private fun initializeLogFile() {
//        val logDir = File(getExternalFilesDir(null), "logs")
//        if (!logDir.exists()) logDir.mkdirs()
//
//        val docFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//
//        logFile = File(docFolder, "keyboard_log.txt")
//        try {
//            if (logFile.exists()) {
//                logFile.delete()
//            }
//            logFile.createNewFile()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    //    private fun logMessage(message: String) {
//        try {
//            logFile.appendText("$message\n")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    private fun initializeLogFile() {
        try {
            val fileName = "keyboard_log.txt"
            val content = "Keyboard session started.\n"

            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/KeyboardLogs")
            }

            logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (logFileUri != null) {
                resolver.openOutputStream(logFileUri!!)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
                logMessage("Log file created at: $logFileUri")
            } else {
                logMessage("Failed to create log file URI.")
            }
        } catch (e: Exception) {
            val stackTrace = StringWriter().also {
                e.printStackTrace(PrintWriter(it))
            }.toString()
            logMessage("Error creating log file: ${e.message}\n$stackTrace")
        }
    }



    private fun logMessage(message: String) {
        try {
            if (logFileUri != null) {
                val resolver = contentResolver
                resolver.openOutputStream(logFileUri!!, "wa")?.use { outputStream ->
                    outputStream.write("$message\n".toByteArray())
                    outputStream.flush()
                }
            } else {
                println("Log URI is null. Message: $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadLanguagePreference() {
        val sharedPreferences = getSharedPreferences("keyboard_prefs", MODE_PRIVATE)
        val selectedLanguage = sharedPreferences.getString("selected_language", "English")
        println("selected Lang=> $selectedLanguage")
        currentLanguageIndex = languages.indexOf(selectedLanguage)
        if (currentLanguageIndex == -1) currentLanguageIndex = 0 // Fallback if saved language is invalid
    }


    private fun switchLanguage() {
        try {
            currentLanguageIndex = (currentLanguageIndex + 1) % languages.size
            val selectedLanguage = languages[currentLanguageIndex]

            val layoutResId = languageLayouts[selectedLanguage] ?: R.xml.keyboard_layout

            keyboard = Keyboard(this, layoutResId)
            keyboardView.keyboard = keyboard
            keyboardView.invalidateAllKeys()

            // saveLanguagePreference()
        } catch (error: Exception) {
            logMessage("Error in onKey: ${error.message}")
            val stackTrace = StringWriter().also {
                error.printStackTrace(PrintWriter(it))
            }.toString()
            logMessage("Error in onKey: ${error.message}\n$stackTrace")
        }
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
        } catch (error: Exception) {
            logMessage("Error in onKey: ${error.message}")
            val stackTrace = StringWriter().also {
                error.printStackTrace(PrintWriter(it))
            }.toString()
            logMessage("Error in onKey: ${error.message}\n$stackTrace")
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

