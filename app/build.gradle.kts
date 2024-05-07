plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    // Other plugins

}

android {
    namespace = "com.intermeet.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.intermeet.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    signingConfigs {
        create("release") {
            keyAlias = "key1"
            keyPassword = "111234"
            storeFile = file("C:\\Users\\spenc\\intermeetSprint1Key.jks")
            storePassword = "111234"
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            // Other release configurations...
        }
    }
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    afterEvaluate {
        val mergeDebugResources = tasks.getByName("mergeDebugResources")
        val processDebugGoogleServices = tasks.getByName("processDebugGoogleServices")
        mergeDebugResources.dependsOn(processDebugGoogleServices)
    }
}

dependencies {
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.core:core-animation:1.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    val lifecycle_version = "2.3.1"
    val fragment_version = "1.6.2"
    val nav_version = "2.3.5"
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.fragment:fragment-ktx:$fragment_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.google.android.material:material:1.11.0")
    // AndroidX Navigation Fragment
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    // AndroidX Navigation UI
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.maps.android:android-maps-utils:2.2.0")
    // add PolyLine dependency
    implementation("com.google.maps:google-maps-services:0.15.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.firebase:geofire-android:3.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.firebase:firebase-appcheck-safetynet:16.1.2")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.1")
    implementation("com.kizitonwose.calendar:view:2.0.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.github.yuyakaido:CardStackView:v2.3.4")
}

