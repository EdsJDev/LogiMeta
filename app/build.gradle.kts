plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ✅ KSP Plugin - compatível com Kotlin 2.0.21
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.example.logimeta"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.logimeta"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//         ✅ Se quiser gerar schemas para Room (opcional, mas recomendado)

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
    }
}

dependencies {
    // Suas dependências existentes
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- DEPENDÊNCIAS DO ROOM ---
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")

    // ✅ Usa KSP em vez de kapt/annotationProcessor
    ksp("androidx.room:room-compiler:$room_version")

    // Extensões do Room com Coroutines
    implementation("androidx.room:room-ktx:$room_version")

    // Testes do Room
    testImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation("androidx.room:room-testing:$room_version")
    // --- FIM DAS DEPENDÊNCIAS DO ROOM ---

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
