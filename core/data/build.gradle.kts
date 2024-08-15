plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
    alias(libs.plugins.nowinkitransferowe.hilt)
    id("kotlinx-serialization")
}

android {
    namespace = "pl.nowinkitransferowe.core.data"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(projects.core.common)
    api(projects.core.database)
    api(projects.core.datastore)
    api(projects.core.network)

    implementation(projects.core.analytics)
    implementation(projects.core.notifications)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.core.datastoreTest)
    testImplementation(projects.core.testing)
}
