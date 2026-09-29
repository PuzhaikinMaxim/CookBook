plugins {
    `kotlin-dsl`
}

group = "com.puj.cookbook.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Bundled on the convention plugins' runtime classpath so they can apply these plugins by id.
    implementation(libs.android.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "cookbook.android.application"
            implementationClass = "com.puj.cookbook.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "cookbook.android.library"
            implementationClass = "com.puj.cookbook.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "cookbook.android.compose"
            implementationClass = "com.puj.cookbook.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "cookbook.android.hilt"
            implementationClass = "com.puj.cookbook.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "cookbook.android.room"
            implementationClass = "com.puj.cookbook.buildlogic.AndroidRoomConventionPlugin"
        }
    }
}
