
plugins {
    alias(libs.plugins.nowinkitransferowe.android.feature)
    alias(libs.plugins.nowinkitransferowe.android.library.compose)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
}
android {
    namespace = "pl.nowinkitransferowe.feature.transfers"
}

dependencies {
    implementation(libs.accompanist.permissions)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)
    testImplementation(projects.core.testing)
    testImplementation(projects.core.screenshotTesting)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(projects.core.testing)
}
