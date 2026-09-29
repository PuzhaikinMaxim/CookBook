plugins {
    alias(libs.plugins.cookbook.android.library)
    alias(libs.plugins.cookbook.android.compose)
    alias(libs.plugins.cookbook.android.hilt)
}

android {
    namespace = "com.puj.cookbook.recipeeditor.ui"
}

dependencies {
    api(project(":feature:recipeeditor:data"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)
}
