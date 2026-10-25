plugins {
    alias(libs.plugins.planningpoker.android.library)
    alias(libs.plugins.planningpoker.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.planningpoker.core.ai"

    // GeminiTechLeadAgent logs failures with android.util.Log, which is a stub on the JVM.
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    api(projects.core.model)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
}
