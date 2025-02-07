@file:Suppress("DEPRECATION")
package com.example.custom_keyboard

import android.content.ContentUris
import android.content.ContentValues
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
        "Russian" to R.xml.russian_layout,
        "Latvian" to R.xml.latvian_layout
    )
    private val languages = languageLayouts.keys.toList()

    private lateinit var logFile: File
    private var logFileUri: Uri? = null
    private var currentLang: String? = "English"
    private var layoutResId: Int = 0


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
        initializeLogFile()

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        val spaceKey = keyboard.keys.firstOrNull { it.codes.contains(62) }
        spaceKey?.label = currentLang

        keyboardView.isPreviewEnabled = false
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

    private fun initializeLogFile() {
        try {
            val fileName = "keyboard_log.txt"
            val content = "Keyboard session started.\n"
            val resolver = contentResolver


            // Query existing file
            val existingFileUri = findExistingLogFileUri(fileName)

            println("existingFileUri=> $existingFileUri")

            if (existingFileUri != null) {
                logFileUri = existingFileUri
                logMessage("Using existing log file: $logFileUri")
            } else {
                // Create new file if it doesn't exist
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
            }
        } catch (e: Exception) {
            val stackTrace = StringWriter().also {
                e.printStackTrace(PrintWriter(it))
            }.toString()
            logMessage("Error creating log file: ${e.message}\n$stackTrace")
        }
    }

    private fun findExistingLogFileUri(fileName: String): Uri? {
        val resolver = contentResolver
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)

        // Fix: Ensure RELATIVE_PATH ends with a slash
        val relativePath = Environment.DIRECTORY_DOCUMENTS + "/KeyboardLogs/"

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ? AND " +
                "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?" // Fix: Use LIKE instead of =
        val selectionArgs = arrayOf(fileName, "$relativePath%") // Fix: Append %

        println("MediaStore.Files.getContentUri => ${MediaStore.Files.getContentUri("external")}")
        println("selection => $selection")
        println("selectionArgs => ${selectionArgs.joinToString()}") // Fix: Print formatted args

        resolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            println("cursor count => ${cursor.count}") // Debugging

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val fileUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                println("Found existing log file URI: $fileUri")
                return fileUri
            }
        }
        return null
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
            logMessage("Error in onKey: ${error.message}")
            val stackTrace = StringWriter().also {
                error.printStackTrace(PrintWriter(it))
            }.toString()
            logMessage("Error in onKey: ${error.message}\n$stackTrace")
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

