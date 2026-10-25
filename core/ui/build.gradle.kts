plugins {
    alias(libs.plugins.planningpoker.android.library)
    alias(libs.plugins.planningpoker.android.compose)
}

android {
    namespace = "com.planningpoker.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
}
