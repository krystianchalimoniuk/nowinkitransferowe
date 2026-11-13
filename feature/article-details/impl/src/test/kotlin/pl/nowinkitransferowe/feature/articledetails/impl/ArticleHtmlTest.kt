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

package pl.nowinkitransferowe.feature.articledetails.impl

import androidx.core.net.toUri
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ArticleHtmlTest {

    @Test
    fun pageUsesTheWebsiteStylesheetAndTheAppTheme() {
        val html = articleHtml("<p>Treść</p>", darkTheme = true)

        assertTrue(html.startsWith("<!doctype html><html data-theme=\"dark\">"))
        assertTrue("<link rel=\"stylesheet\" href=\"/static/css/article.css\">" in html)
        assertTrue("<body><p>Treść</p>" in html)
        assertTrue("data-theme=\"light\"" in articleHtml("", darkTheme = false))
    }

    @Test
    fun linksToWebsiteArticlesGiveTheirId() {
        assertEquals(11284, articleIdOf("https://nowinkitransferowe.pl/11284/transfery/psg-zarzucilo-siec".toUri()))
        assertEquals(11284, articleIdOf("https://www.nowinkitransferowe.pl/11284/x".toUri()))
    }

    @Test
    fun otherLinksGiveNoArticle() {
        assertNull(articleIdOf("https://nowinkitransferowe.pl/klub/legia-warszawa".toUri()))
        assertNull(articleIdOf("https://nowinkitransferowe.pl/".toUri()))
        assertNull(articleIdOf("https://twitter.com/11284/status".toUri()))
    }
}
