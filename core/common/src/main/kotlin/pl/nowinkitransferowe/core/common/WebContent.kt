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

package pl.nowinkitransferowe.core.common

/**
 * Whether screens show web content (the article body) in a WebView. Instrumented tests turn it
 * off with NoWebContentRule: on the API 26 emulator the WebView (Chrome 69) keeps drawing frames
 * through the emulator's GL, so the main thread never goes idle and Espresso waits forever.
 */
object WebContent {
    @Volatile
    var enabled: Boolean = true
}
