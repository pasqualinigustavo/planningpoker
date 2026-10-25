plugins {
    alias(libs.plugins.planningpoker.android.library)
    alias(libs.plugins.planningpoker.android.compose)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.planningpoker.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.core)
}
