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

package pl.nowinkitransferowe.feature.articles.api.navigation

import java.util.concurrent.atomic.AtomicReference

/**
 * A topic the home screen picks when it opens, for a link to /temat/{slug}. The home screen is a
 * top level destination without arguments, so the link leaves the topic here.
 */
object PendingHomeTopic {
    private val topic = AtomicReference<String?>(null)

    fun request(slug: String) = topic.set(slug)

    /** The requested topic, once. */
    fun take(): String? = topic.getAndSet(null)
}
