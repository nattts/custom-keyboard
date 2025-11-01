package com.example.custom_keyboard
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.RadioGroup
import android.widget.RadioButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            println("checkedId => ${selectedRadioButton.text}")
        }
    }
}