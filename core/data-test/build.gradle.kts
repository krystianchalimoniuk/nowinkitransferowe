plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.hilt)
}

android {
    namespace = "pl.nowinkitransferowe.core.data.test"
}

dependencies {
    api(projects.core.data)

    implementation(libs.hilt.android.testing)
}
