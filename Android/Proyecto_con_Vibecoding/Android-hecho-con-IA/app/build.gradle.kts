plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.soa.gympro"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.soa.gympro"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Valores de configuración centralizados (se leen solo desde config/Config.java)
        buildConfigField("String", "API_BASE_URL", "\"https://soa.alfatechnologies.com.ar/\"")
        buildConfigField("String", "MQTT_BROKER_URI", "\"wss://soa.alfatechnologies.com.ar/mqtt\"")
        buildConfigField("String", "TOPIC_OCUPACION", "\"gimnasio/cantidadPersonas\"")
        buildConfigField("String", "TOPIC_ABRIR_PUERTA", "\"gimnasio/puerta/abrirPuerta\"")
        buildConfigField("int", "AFORO_MAXIMO", "50")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.paho)

    testImplementation(libs.junit)
}
