plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.profplay.catchthecorrectcolor"
    compileSdk = 34 // Android 14 (Senin sisteminle uyumlu)

    defaultConfig {
        applicationId = "com.profplay.catchthecorrectcolor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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

    // Kotlin 1.9.0 kullandığın için Compiler 1.5.1 olmalı
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testler
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- JSON & GSON ---
    implementation("com.google.code.gson:gson:2.10.1")

    // 1. BOM (Bill of Materials): Versiyonları bu yönetir.
    // 2024.06.00 sürümü SDK 34 ile tam uyumludur.
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // 2. Compose Kütüphaneleri (Versiyon yazmıyoruz, BOM hallediyor)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Debug için tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 3. Navigation (BOM yönetmez, elle sabitliyoruz)
    // 2.9.6 yerine 2.7.7 kullanıyoruz (SDK 34 dostu)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 4. Lifecycle & ViewModel
    // 2.8.x yerine 2.6.2 (Daha kararlı)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // 5. Activity Compose (Launcher için şart)
    implementation("androidx.activity:activity-compose:1.8.2")

    // 6. LiveData -> State dönüşümü için
    implementation("androidx.compose.runtime:runtime-livedata")

    // ADS SDK (Senin projende vardı, koruyoruz)
    implementation(libs.ads.mobile.sdk)
    implementation("androidx.compose.material:material-icons-extended")
}