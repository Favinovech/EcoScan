import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

// El plugin de Google Services necesita app/google-services.json, que cada persona
// descarga desde la consola de Firebase y no se versiona (ver .gitignore). Si el archivo
// no está presente (por ejemplo, un checkout nuevo o CI sin secretos configurados), no
// aplicamos ni ese plugin ni el de Crashlytics para que el proyecto siga compilando; las
// dependencias de Firebase igual quedan declaradas más abajo.
val googleServicesFile = file("google-services.json")
if (googleServicesFile.exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Clave de Google Maps (HU07). Cada persona la define en local.properties (no se versiona):
//   MAPS_API_KEY=AIza...
// Sin clave la app compila, pero el mapa se ve en blanco.
val mapsApiKey: String = Properties().let { properties ->
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { properties.load(it) }
    properties.getProperty("MAPS_API_KEY", "")
}

android {
    namespace = "pe.ecoscan.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.ecoscan.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // HU09: FirebaseFirestoreException.Code inicializa un SparseArray de android.util en un
    // bloque estático; sin esto, las pruebas unitarias (JVM, sin Robolectric) que lo tocan
    // fallan con "Method get in android.util.SparseArray not mocked", aunque en el dispositivo
    // real funciona sin problema. No afecta el comportamiento de la app, solo cómo corren los
    // tests: cualquier llamada a Android sin mockear devuelve un valor por defecto.
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    // BORRAR ESTO DE AQUÍ:
    androidResources {
        noCompress += "tflite"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // TensorFlow Lite
    implementation(libs.tensorflow.lite)

    // ML Kit - lectura de códigos de barras
    implementation(libs.mlkit.barcode.scanning)

    // Mapa y ubicación (HU07)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    // WorkManager (HU09): recordatorios y sincronización en segundo plano
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    androidTestImplementation(libs.androidx.work.testing)

    // DataStore (HU09, parte 2): preferencias de sonido/vibración de las notificaciones
    implementation(libs.androidx.datastore.preferences)
}

