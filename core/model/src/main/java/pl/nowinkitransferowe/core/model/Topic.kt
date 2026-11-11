/*
 * Copyright 2026 The Android Open Source Project
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

package pl.nowinkitransferowe.core.model

/**
 * A topic articles are filed under, for example a league ("premier-league") or "polacy-za-granica".
 */
data class Topic(
    val slug: String,
    val name: String,
    val kind: TopicKind,
)

enum class TopicKind {
    LEAGUE,
    CUP,
    SPECIAL,
    UNKNOWN,
    ;

    companion object {
        fun fromApi(value: String?): TopicKind = entries.find { it.name == value } ?: UNKNOWN
    }
}
