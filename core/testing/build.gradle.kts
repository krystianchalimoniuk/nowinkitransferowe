
plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.hilt)
}

android {
    namespace = "pl.nowinkitransferowe.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(projects.core.analytics)
    api(projects.core.common)
    api(projects.core.data)
    api(projects.core.model)
    api(projects.core.notifications)


    implementation(libs.androidx.test.rules)
    implementation(libs.hilt.android.testing)
    implementation(libs.kotlinx.datetime)
}
