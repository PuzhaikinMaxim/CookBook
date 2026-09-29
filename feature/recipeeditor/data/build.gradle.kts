plugins {
    alias(libs.plugins.cookbook.android.library)
    alias(libs.plugins.cookbook.android.hilt)
}

android {
    namespace = "com.puj.cookbook.recipeeditor.data"
}

dependencies {
    api(project(":feature:recipes:data"))
}
