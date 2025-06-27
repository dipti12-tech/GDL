plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.map.secret)
    alias(libs.plugins.google.services)
}


android {
    namespace = "com.app.gdl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.gdl"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Lifecycle components
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.retrofit)
    implementation (libs.github.glide)

    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    // Coil (recommended for modern apps)
    implementation (libs.coil)
    implementation (libs.androidx.recyclerview)
    implementation (libs.androidx.viewpager2)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.messaging.ktx)
    implementation (libs.firebase.analytics.ktx)
    implementation (libs.firebase.auth.ktx)
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Coroutines
    implementation (libs.kotlinx.coroutines.android.v171)

    // Room (local database)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    implementation (libs.google.places)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.google.maps)
    implementation(libs.play.services.location)
    implementation(libs.google.places)
    implementation(libs.coil.v240)
    implementation(libs.coil.svg)
    implementation (libs.gson)


}