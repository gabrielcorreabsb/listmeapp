plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Adicione o kapt se precisar de processamento de anotações
    id("kotlin-kapt")
}

android {
    namespace = "com.example.listmeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.listmeapp"
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
        compose = true
        // Adicione viewBinding se precisar usar views XML também
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Ajuste para a versão compatível com seu Kotlin
    }
}

dependencies {
    // Dependências existentes
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navegação Compose
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Retrofit para chamadas de API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coil para carregamento de imagens
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ViewModel para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Accompanist para UI
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Hilt para injeção de dependência (opcional)
    // implementation("com.google.dagger:hilt-android:2.48")
    // kapt("com.google.dagger:hilt-android-compiler:2.48")
    // implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // DataStore para armazenamento de preferências
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}