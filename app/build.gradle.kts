plugins {
    id("com.android.application")
}
android {
    namespace = "ar.com.jorgeabella.japrompts"
    compileSdk = 35
    defaultConfig {
        applicationId = "ar.com.jorgeabella.ketotucuman"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
}
