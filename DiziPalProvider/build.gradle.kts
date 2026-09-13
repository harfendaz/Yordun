plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.lagradost.cloudstream3.gradle")
}

android {
    namespace = "com.dizipal"
    compileSdk = 34

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
    authors = listOf("YourName")
    type = com.lagradost.cloudstream3.gradle.PluginType.Provider
}

dependencies {
    val cloudstream3Version = "master-SNAPSHOT"
    implementation("com.github.recloudstream:cloudstream3:$cloudstream3Version")
    implementation("com.github.recloudstream:javagg:1.0.1")
    implementation("org.jsoup:jsoup:1.17.2")
}
