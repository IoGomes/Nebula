plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "Nebula.Android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nebulamsg.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
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

    buildFeatures{
        viewBinding = true
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.recyclerview)
    implementation(libs.stompprotocolandroid)
    implementation(libs.barcode.scanning)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie)
    implementation(libs.gson)
    implementation(libs.rxandroid)
    implementation(libs.okhttp)
    implementation(libs.gson.v2101)
    implementation(libs.core)
    implementation(libs.socket.io.client)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)}


