package com.intermeet.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.intermeet.android.helperFunc.getUserDataRepository

class EmailActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

//        val emailEdit : EditText = findViewById(R.id.email_input)
//        val email = emailEdit.text.toString()

        ButtonFunc()
    }

    private
    fun ButtonFunc()
    {

        val nextButton: Button = findViewById(R.id.next_button)
        nextButton.setOnClickListener{
            val emailEdit : EditText = findViewById(R.id.email_input)
            val email = emailEdit.text.toString()

            // Retrieve userDataRepository
            val userDataRepository = getUserDataRepository()
            val userData = UserDataRepository.userData ?: UserDataModel()

            userData.email = email
            val intent = Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
