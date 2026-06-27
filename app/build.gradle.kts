import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.implementation
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.conveniar_coordenador"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.conveniar_coordenador"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //Parte que necessária para buscar informações do local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "API_KEY", "\"${properties.getProperty("API_KEY")}\"")
        buildConfigField("String", "BASE_URL", "\"${properties.getProperty("BASE_URL")}\"")
    }

    buildFeatures {
        // Habilita a geração da classe BuildConfig
        buildConfig = true
        viewBinding = true
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0") //Cliente HTTP principal para consumir a API
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Para converter JSON em Objetos Java
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") //Para imprimir requisições e respostas no Logcat
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") // Para Gráficos
    implementation("androidx.work:work-runtime:2.9.0") //para fazer requisições a API em segundo plano
    implementation("androidx.security:security-crypto:1.1.0-alpha06")//para guardar as informações de login de forma encriptada

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
}