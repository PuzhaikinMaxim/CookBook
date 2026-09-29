plugins {
    alias(libs.plugins.cookbook.android.library)
    alias(libs.plugins.cookbook.android.room)
    alias(libs.plugins.cookbook.android.hilt)
}

android {
    namespace = "com.puj.cookbook.recipes.data"
}
