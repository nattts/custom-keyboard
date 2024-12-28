@file:Suppress("DEPRECATION")

package com.example.custom_keyboard
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {

    private lateinit var keyboardView: KeyboardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Load your activity layout
        keyboardView = findViewById(R.id.keyboard_view)

        val keyboard = Keyboard(this, R.xml.keyboard_layout)  // Reference to keyboard_layout.xml
        keyboardView.keyboard = keyboard
    }
}
