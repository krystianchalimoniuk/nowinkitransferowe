plugins {
    alias(libs.plugins.nowinkitransferowe.android.feature)
    alias(libs.plugins.nowinkitransferowe.android.library.compose)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
}

android {
    namespace = "pl.nowinkitransferowe.feature.settings"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.google.oss.licenses)
    implementation(projects.core.data)

    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}
