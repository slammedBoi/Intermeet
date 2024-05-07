package com.intermeet.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.intermeet.android.helperFunc.getUserDataRepository
import java.lang.StringBuilder


class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sb = StringBuilder()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Moved this to button func so that data is extracted when the button is selected
//        val firstEdit: EditText = findViewById<EditText?>(R.id.signupUsername)
//        val lastEdit: EditText = findViewById(R.id.signupUsername2)

//        val firstName = firstEdit.text.toString()
//        val lastName = lastEdit.text.toString()

//        sb.append(firstName).append(" ").append(lastName)
//        val entireName = sb.toString()

        ButtonFunc()
    }

    private
    fun ButtonFunc() {
        val nextButton: Button = findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            val firstEdit: EditText = findViewById(R.id.signupUsername)
            val lastEdit: EditText = findViewById(R.id.signupUsername2)

            val firstName = firstEdit.text.toString()
            val lastName = lastEdit.text.toString()

            // Retrieve userDataRepository
            val userDataRepository = getUserDataRepository()
            if (UserDataRepository.userData == null) {
                UserDataRepository.userData = UserDataModel()
            }

            UserDataRepository.userData?.let { userData ->
                userData.firstName = firstName
                userData.lastName = lastName
            }

            val intent = Intent(this, EmailActivity::class.java)
            startActivity(intent)
        }
    }
}