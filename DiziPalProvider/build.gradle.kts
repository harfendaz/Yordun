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
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

cloudstream {
    language = "tr"
    description = "DiziPal"
    authors = listOf("harfendaz")
    setRepo("harfendaz/Yordun")
}

dependencies {
    val cloudstream by configurations

    cloudstream("com.lagradost:cloudstream3:pre-release")

    implementation("com.github.Blatzar:NiceHttp:0.4.11")
    implementation("org.jsoup:jsoup:1.18.3")
}