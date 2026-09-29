plugins {
    alias(libs.plugins.cookbook.android.library)
    alias(libs.plugins.cookbook.android.compose)
    alias(libs.plugins.cookbook.android.hilt)
}

android {
    namespace = "com.puj.cookbook.recipes.ui"
}

dependencies {
    api(project(":feature:recipes:data"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.hilt.navigation.compose)
}
