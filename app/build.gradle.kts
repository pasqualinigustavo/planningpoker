import java.util.Properties

plugins {
    alias(libs.plugins.planningpoker.android.application)
    alias(libs.plugins.planningpoker.android.compose)
    alias(libs.plugins.planningpoker.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.isFile) file.inputStream().use(::load)
}

fun signingValue(property: String, environmentVariable: String): String? = keystoreProperties.getProperty(property)
    ?: providers.environmentVariable(environmentVariable).orNull

val releaseStoreFile = signingValue("storeFile", "PLANNINGPOKER_KEYSTORE_FILE")

android {
    namespace = "com.planningpoker"

    defaultConfig {
        applicationId = "com.planningpoker"
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = signingValue("storePassword", "PLANNINGPOKER_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "PLANNINGPOKER_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "PLANNINGPOKER_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        // Debug and release share one keystore so both report the same SHA-1/SHA-256,
        // letting API keys be restricted to a single fingerprint.
        debug {
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.feature.sprints)
    implementation(projects.feature.tasks)
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
}
