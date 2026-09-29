package com.puj.cookbook.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Version catalog shared with the main build (wired up in `build-logic/settings.gradle.kts`).
 * Exposed here so convention plugins can reference the same library aliases as the app modules.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Applies the Android settings that every module in this project shares: compile/min SDK levels
 * and the Java compatibility level. Namespace, applicationId and build types stay in each module
 * because they are module specific.
 */
internal fun Project.configureAndroidCommon() {
    val android = extensions.getByType<CommonExtension>()
    android.compileSdk {
        version = release(COMPILE_SDK)
    }
    android.defaultConfig.minSdk = MIN_SDK
    android.compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    android.compileOptions.targetCompatibility = JavaVersion.VERSION_11
}

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 24
