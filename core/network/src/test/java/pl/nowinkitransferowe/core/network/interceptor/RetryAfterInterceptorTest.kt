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

class RetryAfterInterceptorTest {

    private val server = MockWebServer()
    private val sleeps = mutableListOf<Long>()
    private val client = OkHttpClient.Builder()
        .addInterceptor(RetryAfterInterceptor(sleep = { sleeps += it }))
        .build()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun waits_for_retry_after_and_retries() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "2"))
        server.enqueue(MockResponse().setBody("[]"))

        val response = call()

        assertEquals(200, response.code)
        assertEquals(listOf(2_000L), sleeps)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun gives_up_after_three_retries() {
        repeat(4) { server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "1")) }

        val response = call()

        assertEquals(429, response.code)
        assertEquals(listOf(1_000L, 1_000L, 1_000L), sleeps)
        assertEquals(4, server.requestCount)
    }

    @Test
    fun waits_five_seconds_without_retry_after() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setBody("[]"))

        call()

        assertEquals(listOf(5_000L), sleeps)
    }

    @Test
    fun caps_a_long_retry_after_at_one_minute() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "3600"))
        server.enqueue(MockResponse().setBody("[]"))

        call()

        assertEquals(listOf(60_000L), sleeps)
    }

    @Test
    fun other_errors_are_not_retried() {
        server.enqueue(MockResponse().setResponseCode(503))

        val response = call()

        assertEquals(503, response.code)
        assertEquals(emptyList(), sleeps)
        assertEquals(1, server.requestCount)
    }

    private fun call() = client.newCall(Request.Builder().url(server.url("/api/v3/topics")).build())
        .execute()
        .also { it.close() }
}
