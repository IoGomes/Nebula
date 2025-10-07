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

    //Lottie - Biblioteca Debug para detectação de Memory Leaks;

    //Lottie - Biblioteca para Animações;
    implementation(libs.lottie)

    //Retrofit - Bibliotecas para Requisição REST;
    implementation(libs.retrofit.v2110)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.androidx.appcompat.v170)

    implementation(libs.androidx.core.splashscreen)

    val camerax_version = "1.3.1"
    implementation(libs.androidx.appcompat.v161)

    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
}