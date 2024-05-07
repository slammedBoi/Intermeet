package com.intermeet.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EmailGrabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        ButtonFunc()
    }

    private fun ButtonFunc()
    {
        val nextButton: Button = findViewById(R.id.ForgotPasswordButton)
        nextButton.setOnClickListener{
            val emailForPass : EditText = findViewById(R.id.emailForgotPass)
            val email = emailForPass.text.toString()

            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}