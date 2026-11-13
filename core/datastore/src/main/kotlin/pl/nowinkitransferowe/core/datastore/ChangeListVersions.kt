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

package pl.nowinkitransferowe.core.datastore

/**
 * Class summarizing the local version of each model for sync.
 *
 * The versions are the last applied versions of the API v3 change lists, where 0 means that
 * nothing was synced yet. The API v2 versions stay unused in the proto.
 */
data class ChangeListVersions(
    val articleVersion: Int = 0,
    val transferVersion: Int = 0,
    val rumourVersion: Int = 0,
    val clubVersion: Int = 0,
    val playerVersion: Int = 0,
)
