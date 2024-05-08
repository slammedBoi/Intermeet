package com.intermeet.android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class ProfileEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var verifyEmailButton: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_email)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        emailInput = findViewById(R.id.email_input)
        verifyEmailButton = findViewById(R.id.verify_email)

        // Set the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        verifyEmailButton.setOnClickListener {
            val newEmail = emailInput.text.toString().trim()
            if (newEmail.isNotEmpty()) {
                updateEmail(newEmail)
            } else {
                Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmail(newEmail: String) {
        auth.currentUser?.let { user ->
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update email: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("ProfileEmailActivity", "Failed to update email", task.exception)
                }
            }
        } ?: Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // This will navigate the user back to the previous Activity
        return true
    }
}
