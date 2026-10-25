plugins {
    alias(libs.plugins.planningpoker.android.library)
    alias(libs.plugins.planningpoker.android.hilt)
}

android {
    namespace = "com.planningpoker.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.ai)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
