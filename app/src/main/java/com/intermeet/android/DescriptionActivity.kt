package com.intermeet.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.intermeet.android.helperFunc.getUserDataRepository

class DescriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_descriptioin)

        buttonFunc()
    }

    private
    fun buttonFunc(){
        val nextButton: Button = findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            // store user input of description
            val descriptionEdit : EditText = findViewById(R.id.IntroText)
            val description = descriptionEdit.text.toString()

            // Retrieve userDataRepository
            val userDataRepository = getUserDataRepository()
            val userData = UserDataRepository.userData ?: UserDataModel()

            userData.aboutMeIntro = description

            // Intent to navigate to the SecondActivity
            val intent = Intent(this, PromptsActivity::class.java)
            startActivity(intent)
        }
}
}