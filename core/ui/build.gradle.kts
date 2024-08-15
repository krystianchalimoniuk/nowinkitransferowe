plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.android.library.compose)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "pl.nowinkitransferowe.core.ui"
}

dependencies {
    api(libs.androidx.metrics)
    api(projects.core.analytics)
    api(projects.core.designsystem)
    api(projects.core.model)

    implementation(libs.androidx.browser)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(projects.core.testing)
}
