import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

fun keysProperty(key: String, default: String = "") : String {
    val props = Properties()
    val file = File(rootProject.projectDir,"keys.properties")
    if (file.exists()) FileInputStream(file).use { props.load(it) }
    return props.getProperty(key, default)
}

android {
    namespace = "dev.korryr.epesa"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.korryr.epesa"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CONSUMER_KEY",        "\"${keysProperty("mpesa.consumerKey")}\"")
        buildConfigField("String", "CONSUMER_SECRET",     "\"${keysProperty("mpesa.consumerSecret")}\"")
        buildConfigField("String", "PASSKEY",             "\"${keysProperty("mpesa.passkey")}\"")
        buildConfigField("String", "CALLBACK_URL",        "\"${keysProperty("mpesa.callbackUrl")}\"")
        buildConfigField("String", "BUSINESS_SHORT_CODE", "\"${keysProperty("mpesa.businessShortCode")}\"")
        buildConfigField("String", "MPESA_BASE_URL",      "\"https://sandbox.safaricom.co.ke/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Timber for logging
    implementation(libs.timber)

    // Navigation
    implementation(libs.androidx.hilt.navigation.compose)
}
