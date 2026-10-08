import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing: CI passes CS_* env vars (from GitHub secrets); locally we read
// ~/.courtside/keystore.properties. The keystore never lives in the repo.
val signingProps = Properties().apply {
    val f = File(System.getProperty("user.home"), ".courtside/keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signing(env: String, prop: String): String? = System.getenv(env) ?: signingProps.getProperty(prop)

// Every CI build gets a higher versionCode so Obtainium sees it as an update.
val buildNumber = (System.getenv("CS_BUILD_NUMBER") ?: "0").toInt()

android {
    namespace = "com.nunna.courtside"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nunna.courtside"
        minSdk = 28
        targetSdk = 36
        versionCode = 100 + buildNumber
        versionName = "0.1.$buildNumber"
    }

    signingConfigs {
        create("release") {
            val store = signing("CS_KEYSTORE_FILE", "storeFile")
            if (store != null) {
                storeFile = file(store)
                storePassword = signing("CS_KEYSTORE_PASSWORD", "storePassword")
                keyAlias = signing("CS_KEY_ALIAS", "keyAlias")
                keyPassword = signing("CS_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint { checkReleaseBuilds = false }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
