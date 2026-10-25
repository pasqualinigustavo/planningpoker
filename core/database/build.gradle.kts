plugins {
    alias(libs.plugins.planningpoker.android.library)
    alias(libs.plugins.planningpoker.android.hilt)
    alias(libs.plugins.planningpoker.android.room)
}

android {
    namespace = "com.planningpoker.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}
