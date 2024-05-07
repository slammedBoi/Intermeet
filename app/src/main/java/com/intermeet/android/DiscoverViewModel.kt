package com.intermeet.android

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import queryNearbyUsers
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class DiscoverViewModel : ViewModel() {
    val nearbyUserIdsLiveData = MutableLiveData<List<String>>()
    private val _nearbyUserIdsLiveData = MutableLiveData<List<String>>()

    private val _userData = MutableLiveData<UserDataModel?>()
    val userData: MutableLiveData<UserDataModel?> = _userData

    val filteredUserIdsLiveData = MutableLiveData<List<String>>()

    fun fetchUserData(userId: String) {
        viewModelScope.launch {
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserDataModel::class.java)
                    _userData.postValue(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiscoverViewModel", "Failed to read user data", error.toException())
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAndFilterUsers() {
        fetchCurrentUserLocationAndQueryNearbyUsers()  // Fetch nearby users first
        _nearbyUserIdsLiveData.observeForever { userIds ->
            filterUserIdsByPreference(userIds)  // Filter them once fetched
        }
    }

    private fun fetchCurrentUserLocationAndQueryNearbyUsers() {
        // Obtain the current user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Reference to the user's data in Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                val maxDistancePreference =
                    (snapshot.child("maxDistancePreference").getValue(Double::class.java)
                        ?.times(1.609))
                        ?: 10.0

                if (latitude != null && longitude != null) {
                    // Launch a coroutine in the ViewModelScope
                    viewModelScope.launch {
                        try {
                            val nearbyUserIds =
                                queryNearbyUsers(latitude, longitude, maxDistancePreference).filter { it != userId }
                            _nearbyUserIdsLiveData.postValue(nearbyUserIds)
                        } catch (e: Exception) {
                            Log.e("DiscoverViewModel", "Error querying nearby users", e)
                        }
                    }
                } else {
                    Log.e("DiscoverViewModel", "User location is missing or invalid.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "DiscoverViewModel",
                    "Failed to read user location: ",
                    databaseError.toException()
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterUserIdsByPreference(userIds: List<String>) {
        viewModelScope.launch {
            val currentUser = fetchCurrentUserPreferences()
            val seenUserIds = fetchSeenUserIds()
            val userRef = FirebaseDatabase.getInstance().getReference("users")

            val usersDataDeferred = userIds.map { userId ->
                async {
                    val userData =
                        userRef.child(userId).get().await().getValue(UserDataModel::class.java)
                    if (userData != null && !seenUserIds.contains(userId)) {
                        userId to userData
                    } else {
                        null
                    }
                }
            }

            val usersData = usersDataDeferred.awaitAll().filterNotNull().toMap()

            // Filter and sort the users
            val filteredAndSortedIds : MutableList<String> = usersData.filter {
                userMeetsPreferences(it.value, currentUser)
            }.toList().sortedByDescending {
                commonInterestsCount(it.second.interests, currentUser.interests)
            }.map { it.first }.toMutableList()

            var filterOutLikes : List<String> = emptyList()
            fetchLikedUsers(getCurrentUser()!!){ filteredUsers ->
                filterOutLikes = filteredUsers
            }

            for(i in filteredAndSortedIds)
            {
                if(filterOutLikes.contains(i))
                {
                    filteredAndSortedIds.removeAt(filteredAndSortedIds.indexOf(i))
                }
            }

            filteredUserIdsLiveData.postValue(filteredAndSortedIds)
        }
    }

    private fun getCurrentUser() : String?
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        return userId
    }

    private fun fetchLikedUsers(userID: String, callback: (List<String>) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("likes")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedUserIds = snapshot.children.mapNotNull { it.key }
                callback(likedUserIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchLikedUsers", "Error fetching liked user IDs: ${error.message}")
                callback(emptyList()) // Return an empty list in case of error
            }
        })
    }

    private suspend fun fetchCurrentUserPreferences(): UserDataModel {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return UserDataModel()
        val prefRef = FirebaseDatabase.getInstance().getReference("users/$userId")
        return prefRef.get().await().getValue(UserDataModel::class.java) ?: UserDataModel()
    }

    private suspend fun fetchSeenUserIds(): Set<String> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptySet()
        val seenRef = FirebaseDatabase.getInstance().getReference("users/$userId/seen")

        return try {
            val snapshot = seenRef.get().await()
            snapshot.children.mapNotNull { it.key }.toSet()
        } catch (e: Exception) {
            Log.e("DiscoverViewModel", "Failed to fetch seen user IDs", e)
            emptySet()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun userMeetsPreferences(user: UserDataModel, currentUser: UserDataModel): Boolean {

        val preferenceFields = listOf(
            "smokingPreference", "ethnicityPreference", "politicsPreference", "drugsPreference", "drinkingPreference", "religionPreference"
        )

        var score = 0
        val user_name = user.firstName
        Log.d("DiscoverViewModel", "Score rn $user_name")

        // Assume ageWithinRange and doesGenderMatch methods are defined elsewhere
        if (!ageWithinRange(user.birthday, currentUser.minAgePreference, currentUser.maxAgePreference)) {
            return false
            Log.d("DiscoverViewModel", "Score rn $score")

        }

        if (currentUser.genderPreference != "Open to all" && !doesGenderMatch(user.gender, currentUser.genderPreference)) {
            return false
        }

        for (prefField in preferenceFields) {
            val userValue = user::class.java.getDeclaredField(prefField).apply { isAccessible = true }.get(user) as String
            val currentUserPreference = currentUser::class.java.getDeclaredField(prefField).apply { isAccessible = true }.get(currentUser) as String

            if (currentUserPreference == "Open to all" || currentUserPreference == userValue) {
                score++
                Log.d("DiscoverViewModel", "Score rn $score")
            }
        }
        Log.d("DiscoverViewModel", "Total Score rn ${score}")
        return score >= 3


    }

    private fun doesGenderMatch(userGender: String?, userPreference: String?): Boolean {
        return when (userPreference) {
            "Men" -> userGender == "Male"
            "Women" -> userGender == "Female"
            "Nonbinary" -> userGender in listOf("Nonbinary", "Trans")
            "Everyone" -> true
            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ageWithinRange(birthday: String?, minAge: Int?, maxAge: Int?): Boolean {
        if (birthday == null) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val birthDate = LocalDate.parse(birthday, formatter)
            val now = LocalDate.now()
            val age = Period.between(birthDate, now).years
            (minAge == null || age >= minAge) && (maxAge == null || age <= maxAge)
        } catch (e: Exception) {
            Log.e("DiscoverViewModel", "Error parsing date: $birthday", e)
            false
        }
    }

    fun addLike(likedUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val likeTimestamp = System.currentTimeMillis()
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$likedUserId/likes")
        dbRef.updateChildren(mapOf(userId to likeTimestamp))

        // Notify liked user
        val notificationRef = FirebaseDatabase.getInstance().getReference("users/$likedUserId/notifications")
        val notification = mapOf(
            "fromUserId" to userId,
            "type" to "like",
            "timestamp" to likeTimestamp
        )
        notificationRef.push().setValue(notification)
            .addOnSuccessListener {
                Log.d(TAG, "Notification successfully created for user $likedUserId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to create notification for user $likedUserId", it)
            }

    }


    fun markAsSeen(seenUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val seenTimestamp = System.currentTimeMillis()
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/seen")
        dbRef.updateChildren(mapOf(seenUserId to seenTimestamp))
    }

    private fun commonInterestsCount(userInterests: List<String>?, currentUserInterests: List<String>?): Int {
        if (userInterests == null || currentUserInterests == null) {
            Log.e("DiscoverViewModel", "One or both users have null interests")
            return 0
        }

        val commonInterests = userInterests.intersect(currentUserInterests.toSet())
        Log.d("DiscoverViewModel", "Comparing interests: User interests = ${userInterests.joinToString()}, Current user interests = ${currentUserInterests.joinToString()}")
        Log.d("DiscoverViewModel", "Common interests: ${commonInterests.joinToString()}")
        Log.d("DiscoverViewModel", "Common interests count: ${commonInterests.size}")
        return commonInterests.size
    }

    fun clearSeenUsers() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val seenRef = FirebaseDatabase.getInstance().getReference("users/$userId/seen")
        seenRef.removeValue()
    }
}
