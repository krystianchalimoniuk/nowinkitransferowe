plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.hilt)
}

android {
    namespace = "pl.nowinkitransferowe.core.sync.test"
}

dependencies {
    implementation(libs.hilt.android.testing)
    implementation(projects.core.data)
    implementation(projects.sync.work)
}
