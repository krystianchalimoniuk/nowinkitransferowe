plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
    alias(libs.plugins.nowinkitransferowe.android.room)
    alias(libs.plugins.nowinkitransferowe.hilt)

}


android {
    namespace = "pl.nowinkitransferowe.core.database"
}

dependencies {
    api(projects.core.model)

    implementation(libs.kotlinx.datetime)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
