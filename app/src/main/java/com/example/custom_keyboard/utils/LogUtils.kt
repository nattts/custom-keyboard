package com.example.custom_keyboard.utils
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.PrintWriter
import java.io.StringWriter

fun logMessage(context: Context, logFileUri: Uri?, message: String) {
    try {
        if (logFileUri != null) {
            context.contentResolver.openOutputStream(logFileUri!!, "wa")?.use { outputStream ->
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

fun getStackTrace(error: Exception): String {
    return StringWriter().also {
        error.printStackTrace(PrintWriter(it))
    }.toString()
}

fun initializeLogFile(context: Context, logFileUri: Uri?) {
    try {
        val fileName = "keyboard_log.txt"
        val content = "Keyboard session started.\n"

        // Query existing file
        val existingFileUri = findExistingLogFileUri(context, fileName)

        if (existingFileUri != null) {
            logMessage(context, logFileUri, "Using existing log file: $existingFileUri")
        } else {
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/KeyboardLogs")
            }

            val fileUri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (fileUri != null) {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
                logMessage(context, fileUri, "Log file created at: $fileUri")
            } else {
                logMessage(context, null, "Failed to create log file URI.")
            }
        }
    } catch (error: Exception) {
        logMessage(context, logFileUri, "Error in initializeLogFile: ${error.message}")
        logMessage(context, logFileUri, "Error initializeLogFile stacktrace: ${getStackTrace(error)}")
    }
}

private fun findExistingLogFileUri(context: Context, fileName: String): Uri? {
    val projection = arrayOf(MediaStore.Files.FileColumns._ID)

    // Fix: Ensure RELATIVE_PATH ends with a slash
    val relativePath = Environment.DIRECTORY_DOCUMENTS + "/KeyboardLogs/"

    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ? AND " +
            "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?" // Fix: Use LIKE instead of =
    val selectionArgs = arrayOf(fileName, "$relativePath%") // Fix: Append %

    println("MediaStore.Files.getContentUri => ${MediaStore.Files.getContentUri("external")}")
    println("selection => $selection")
    println("selectionArgs => ${selectionArgs.joinToString()}") // Fix: Print formatted args

    context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        println("cursor => $cursor")
        println("cursor count => ${cursor.count}")


        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val fileUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            println("Found existing log file URI: $fileUri")
            return fileUri
        }
    }
    return null
}