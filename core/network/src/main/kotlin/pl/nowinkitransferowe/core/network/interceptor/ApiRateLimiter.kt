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

package pl.nowinkitransferowe.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Spaces out requests to the API so that one device stays under the server limit of 300 requests
 * a minute, even during the first full sync. Only paths under [pathPrefix] are limited; images
 * loaded through the same client are not.
 */
class ApiRateLimiter(
    private val minIntervalMillis: Long = 250,
    private val pathPrefix: String = "/api/",
    private val nowMillis: () -> Long = { TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) },
    private val sleep: (millis: Long) -> Unit = Thread::sleep,
) : Interceptor {

    private val lock = Any()
    private var nextAllowedAt = Long.MIN_VALUE

    override fun intercept(chain: Interceptor.Chain): Response {
        if (chain.request().url.encodedPath.startsWith(pathPrefix)) {
            val wait = synchronized(lock) {
                val now = nowMillis()
                val start = maxOf(now, nextAllowedAt)
                nextAllowedAt = start + minIntervalMillis
                start - now
            }
            if (wait > 0) sleep(wait)
        }
        return chain.proceed(chain.request())
    }
}
