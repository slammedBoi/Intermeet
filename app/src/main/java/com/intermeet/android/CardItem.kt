package com.intermeet.android

data class CardItem(
    val name: String,
    val imageUrl: String,
    val education: String,
    val location: String,
    val pronouns: String,
    val gender: String,
    val sexuality: String,
    val ethnicity: String,
    val height: String,
    val aboutMe: String,
    val interests: List<String>  // Assuming interests are represented as a list of strings
)
