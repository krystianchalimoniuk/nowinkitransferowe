/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.build.api.variant.BuildConfigField
import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.nowinkitransferowe.android.library)
    alias(libs.plugins.nowinkitransferowe.android.library.jacoco)
    alias(libs.plugins.nowinkitransferowe.hilt)
    id("kotlinx-serialization")
}

android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "pl.nowinkitransferowe.core.network"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    api(libs.kotlinx.datetime)
    api(projects.core.common)
    api(projects.core.model)

    implementation(libs.coil.kt)
    implementation(libs.coil.kt.svg)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)

    testImplementation(libs.kotlinx.coroutines.test)
}

// URLs come from secrets.defaults.properties and can be overridden in local.properties.
// Values are Java string literals, so they keep their surrounding quotes.
val urlProperties = listOf("secrets.defaults.properties", "local.properties")
    .map { fileName ->
        providers.fileContents(isolated.rootProject.projectDirectory.file(fileName))
            .asText.orElse("")
    }
    .reduce { defaults, overrides -> defaults.zip(overrides) { d, o -> "$d\n$o" } }
    .map { text -> Properties().apply { load(StringReader(text)) } }

androidComponents {
    onVariants { variant ->
        listOf("BASE_URL", "BACKEND_URL", "IMAGES_URL").forEach { name ->
            variant.buildConfigFields!!.put(
                name,
                urlProperties.map { properties ->
                    BuildConfigField(type = "String", value = properties.getProperty(name), comment = null)
                },
            )
        }
    }
}
