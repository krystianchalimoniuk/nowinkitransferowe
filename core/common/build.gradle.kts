plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.hilt)
}

android {
    namespace = "pl.nowinkitransferowe.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}