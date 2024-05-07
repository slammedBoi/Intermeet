package com.intermeet.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.intermeet.android.R

class ResetPasswordActivity: AppCompatActivity() {
    private var newPassword: String? = null
    private lateinit var backButton: Button


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password)
        backButton = findViewById(R.id.ForgotPasswordButton)
        val newPasswordInput : EditText = findViewById(R.id.newPassword)
        val confirmPasswordInput : EditText = findViewById(R.id.confirm_Password)
        newPassword = newPasswordInput.text.toString()
        backButton.setOnClickListener {
            // Intent to navigate to the SecondActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}