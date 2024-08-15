plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.android.library.compose)
    alias(libs.plugins.nowinkitransferowe.hilt)
}

android {
    namespace = "pl.nowinkitransferowe.core.screenshottesting"
}

dependencies {
    api(libs.bundles.androidx.compose.ui.test)
    api(libs.roborazzi)
    implementation(libs.androidx.compose.ui.test)
    implementation(libs.androidx.activity.compose)
    implementation(libs.robolectric)
    implementation(projects.core.designsystem)
}