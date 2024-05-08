package com.intermeet.android

import android.app.Application
import android.net.Uri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory

class IntermeetApp : Application() {
    val userDataRepository = UserDataRepository
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        setupFirebaseAppCheck()

        setupNotificationChannels()
    }

    private fun setupFirebaseAppCheck() {
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                channelId = "LikeChannel",
                channelName = getString(R.string.like_channel_name),
                channelDescription = getString(R.string.like_channel_description)
            )

            createNotificationChannel(
                channelId = "ChatChannel",
                channelName = getString(R.string.chat_channel_name),
                channelDescription = getString(R.string.chat_channel_description)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, channelDescription: String) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = channelDescription
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}

object UserDataRepository {
    var userData: UserDataModel? = null

    fun clearUserData() {
        userData = null
    }

    fun getUserEmail(userId: String): Any {
        return ""
    }
}



object AppState {
    var isChatFragmentActive: Boolean = false
    var isChatActivityVisible: Boolean = false
    var currentChatUserId: String? = null  // Add this line
}





data class UserDataModel(
    var userId: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var birthday: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var notificationPreferences: Boolean? = null,
    var photoDownloadUrls: MutableList<String> = mutableListOf(),
    var photoUris: MutableList<Uri> = mutableListOf(),
    // User preferences
    var genderPreference: String? = null,
    var maxDistancePreference: Int? = null,
    var religionPreference: String? = null,
    var ethnicityPreference: String? = null,
    var drinkingPreference: String? = null,
    var smokingPreference: String? = null,
    var politicsPreference: String? = null,
    var drugsPreference: String? = null,
    var minAgePreference: Int? = null,
    var maxAgePreference: Int? = null,
    // Additional user-related information
    var school: String? = null,
    var gender: String? = null,
    var height: String? = null,
    var religion: String? = null,
    var drinking: String? = null,
    var drugs: String? = null,
    var smoking: String? = null,
    var politics: String? = null,
    var ethnicity: String? = null,
    var occupation: String? = null,
    var sexuality: String? = null,
    var pronouns: String? = null,
    var interests: List<String> = listOf(),
    // About me section
    var aboutMeIntro: String? = null,
    var prompts: MutableList<String> = mutableListOf(),
    // Discover attributes
    var likes: Map<String, Long> = emptyMap(),
    var seen: Map<String, Long> = emptyMap()
)