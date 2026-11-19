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

package pl.nowinkitransferowe.core.testing.rules

import org.junit.rules.ExternalResource
import pl.nowinkitransferowe.core.common.WebContent

/**
 * Keeps WebViews out of a test (see [WebContent]), so screens with web content can go idle on
 * every emulator. Apply it before the rule that shows the UI.
 */
class NoWebContentRule : ExternalResource() {
    override fun before() {
        WebContent.enabled = false
    }

    override fun after() {
        WebContent.enabled = true
    }
}
