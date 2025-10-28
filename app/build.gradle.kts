plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp) // MANTÉM O KSP
    // id("org.jetbrains.kotlin.kapt") // REMOVIDO
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}

android {
    namespace = "devandroid.moacir.meuorcamento"
    compileSdk = 36

    defaultConfig {
        applicationId = "devandroid.moacir.meuorcamento"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Configuração do KSP para o Room (já estava correta)
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room - Agora usando KSP e as dependências do TOML
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // <<< MUDANÇA PRINCIPAL: DE 'kapt' PARA 'ksp'

    // Animated Vector Drawable
    implementation(libs.androidx.vectordrawable.animated)

    // Gson for Type Converters
    implementation(libs.google.code.gson)

    // ViewModel and Lifecycle
    // Use as versões do TOML se estiverem lá, senão defina aqui
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3") // Versão estável recomendada
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")  // Versão estável recomendada

    // Coroutines
    // Use as versões do TOML se estiverem lá, senão defina aqui
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // A DECLARAÇÃO ANTIGA E DUPLICADA DO ROOM FOI REMOVIDA DAQUI
}
