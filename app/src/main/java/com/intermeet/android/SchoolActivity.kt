package com.intermeet.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.intermeet.android.helperFunc.getUserDataRepository

class SchoolActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school)

        ButtonFunc()
    }

    private fun ButtonFunc() {
        val nextButton: Button = findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            val schoolEdit: EditText = findViewById(R.id.enter_school)
            val school = schoolEdit.text.toString()

            // Retrieve userDataRepository
            val userDataRepository = getUserDataRepository()
            val userData = UserDataRepository.userData ?: UserDataModel()

            userData.school = school
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
        }
    }
}