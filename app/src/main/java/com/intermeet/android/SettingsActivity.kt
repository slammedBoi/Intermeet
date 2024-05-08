package com.intermeet.android

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val RESULT_GO_TO_PROFILE = 1 // A unique code to identify the specific result.
    }
    private fun openNotificationSettingsForApp() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", packageName)
                    putExtra("app_uid", applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:$packageName")
                }
            }
        }
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val emailButton: Button = findViewById(R.id.Email)
        val logout: Button = findViewById(R.id.LogOut)
        val privacyPolicyButton: Button = findViewById(R.id.privacyPolicyButton)
        val notificationSettingsButton: Button = findViewById(R.id.PushNotifications)
        val termsOfServiceButton: Button = findViewById(R.id.TOS)

        // Set click listener for the toolbar navigation icon (back button)
        toolbar.setNavigationOnClickListener {
            // Navigate back to MainActivity and try to show ProfileFragment
            val intent = Intent(this, MainActivity::class.java).apply {
                // Clear all activities on top of MainActivity and bring it to the top
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        // Set click listener for the email button
        emailButton.setOnClickListener {
            // Navigate to the ProfileEmailActivity
            val intent = Intent(this, ProfileEmailActivity::class.java)
            startActivity(intent)
        }
        notificationSettingsButton.setOnClickListener {
            openNotificationSettingsForApp()
        }
        privacyPolicyButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", "https://intermeetiate.github.io/InterMeetiatePrivatePolicy") // Your privacy policy URL
            startActivity(intent)
        }

        termsOfServiceButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", "https://intermeetiate.github.io/TermsOfService/") // Your terms of service URL
            startActivity(intent)
        }
        // Set click listener for the payments button

        logout.setOnClickListener {
            // Show a loading indicator or a simple toast message
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, BeginningActivity::class.java).apply {
                // Clear the activity stack.
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

    }
}
