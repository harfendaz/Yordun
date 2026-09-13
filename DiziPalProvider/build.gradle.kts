plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.lagradost.cloudstream3.gradle")
}

android {
    namespace = "com.dizipal"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

cloudstream {
    language = "tr"
    description = "DiziPal provider for Turkish TV series and movies"
    authors = listOf("harfendaz")
}

dependencies {
    val cloudstream by configurations

    cloudstream("com.lagradost:cloudstream3:pre-release")

    implementation("org.jsoup:jsoup:1.17.2")
}