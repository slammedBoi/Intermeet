package com.intermeet.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BeginningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beginning)

        setupClickableText()
        buttonFunc()
    }

    private fun setupClickableText() {
        val termsAndPrivacyText: TextView = findViewById(R.id.TOSyPP)
        val fullText = getString(R.string.TOSyPP)

        val termsIndex = fullText.indexOf("Terms")
        val privacyIndex = fullText.indexOf("Privacy Policy")

        val spannable = SpannableString(fullText)

        val termsClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Launch WebView or Browser for Terms
                val intent = Intent(this@BeginningActivity, WebViewActivity::class.java)
                intent.putExtra("url", "https://intermeetiate.github.io/TermsOfService/") // Change this to your actual Terms URL
                startActivity(intent)
            }
        }

        val privacyClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Launch WebView or Browser for Privacy Policy
                val intent = Intent(this@BeginningActivity, WebViewActivity::class.java)
                intent.putExtra("url", "https://intermeetiate.github.io/InterMeetiatePrivatePolicy/") // Change this to your actual Privacy Policy URL
                startActivity(intent)
            }
        }

        // Apply the clickable span to the word "Terms"
        if (termsIndex != -1) {
            spannable.setSpan(termsClick, termsIndex, termsIndex + "Terms".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Apply the clickable span to the words "Privacy Policy"
        if (privacyIndex != -1) {
            spannable.setSpan(privacyClick, privacyIndex, privacyIndex + "Privacy Policy".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        termsAndPrivacyText.text = spannable
        termsAndPrivacyText.movementMethod = LinkMovementMethod.getInstance() // This makes the links clickable
    }

    private fun buttonFunc() {
        val signInButton: TextView = findViewById(R.id.sign_in_button)
        val signUpButton: TextView = findViewById(R.id.sign_up_button)

        signInButton.setOnClickListener {
            // Intent to navigate to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            // Intent to navigate to the SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Set up animations for touch interaction
        setupButtonAnimations(signInButton)
        setupButtonAnimations(signUpButton)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtonAnimations(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }
            false // Return false to allow the click event to proceed
        }
    }
}
