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
 * Retries a request the server rejected with `429 Too Many Requests`, after the number of seconds
 * in its `Retry-After` header. After [maxRetries] retries the `429` reaches the caller, and sync
 * retries later with WorkManager's backoff.
 */
class RetryAfterInterceptor(
    private val maxRetries: Int = 3,
    private val maxWaitSeconds: Long = 60,
    private val sleep: (millis: Long) -> Unit = Thread::sleep,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        var retries = 0
        while (response.code == HTTP_TOO_MANY_REQUESTS && retries < maxRetries && !chain.call().isCanceled()) {
            val waitSeconds = response.header("Retry-After")?.trim()?.toLongOrNull()
                ?.coerceIn(0, maxWaitSeconds)
                ?: DEFAULT_WAIT_SECONDS
            response.close()
            sleep(TimeUnit.SECONDS.toMillis(waitSeconds))
            retries++
            response = chain.proceed(chain.request())
        }
        return response
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val DEFAULT_WAIT_SECONDS = 5L
    }
}
