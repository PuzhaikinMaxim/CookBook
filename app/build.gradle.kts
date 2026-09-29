plugins {
    alias(libs.plugins.cookbook.android.application)
    alias(libs.plugins.cookbook.android.compose)
    alias(libs.plugins.cookbook.android.hilt)
}

android {
    namespace = "com.puj.cookbook"

    defaultConfig {
        applicationId = "com.puj.cookbook"
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:recipes:ui"))
    implementation(project(":feature:recipeeditor:ui"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
