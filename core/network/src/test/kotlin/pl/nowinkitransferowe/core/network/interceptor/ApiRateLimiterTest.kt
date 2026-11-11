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

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ApiRateLimiterTest {

    private val server = MockWebServer()
    private var now = 0L
    private val sleeps = mutableListOf<Long>()
    private val client = OkHttpClient.Builder()
        .addInterceptor(
            ApiRateLimiter(
                minIntervalMillis = 250,
                nowMillis = { now },
                sleep = { millis ->
                    sleeps += millis
                    now += millis
                },
            ),
        )
        .build()

    @Before
    fun setUp() {
        server.start()
        repeat(4) { server.enqueue(MockResponse().setBody("[]")) }
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun api_requests_in_a_burst_are_spaced_out() {
        repeat(3) { call("/api/v3/articles?ids=1") }

        assertEquals(listOf(250L, 250L), sleeps)
    }

    @Test
    fun requests_already_spaced_out_do_not_wait() {
        call("/api/v3/articles?ids=1")
        now += 1_000
        call("/api/v3/articles?ids=2")

        assertEquals(emptyList(), sleeps)
    }

    @Test
    fun images_are_not_limited() {
        repeat(3) { call("/media/w/160/liverpool.png.webp") }

        assertEquals(emptyList(), sleeps)
    }

    private fun call(path: String) = client.newCall(Request.Builder().url(server.url(path)).build())
        .execute()
        .close()
}
