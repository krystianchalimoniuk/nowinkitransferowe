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

package pl.nowinkitransferowe.core.data.news

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class EditorsNotificationLinksTest {

    private val rumours = TestRumourRepository()
    private val articles = TestArticleRepository()
    private val transfers = TestTransferRepository().apply { sendTransfers(emptyList()) }

    /** A sync that brings [onSync] when requested. */
    private inner class TestSyncManager(private val onSync: () -> Unit = {}) : SyncManager {
        var requests = 0
        override val isSyncing = MutableStateFlow(false)
        override fun requestSync() {
            requests++
            onSync()
        }
    }

    private val rumour = rumoursTestData[0]
    private val article = articlesTestData[0]
    private val articleUrl = "https://nowinkitransferowe.pl/${article.id}/transfery/slug"

    @Test
    fun articleId_isReadFromTheWebsiteUrl() {
        assertEquals(4321, EditorsNotificationLinks.articleIdFromUrl("https://nowinkitransferowe.pl/4321/transfery/martinelli-w-al-hilal"))
        assertEquals(4321, EditorsNotificationLinks.articleIdFromUrl("https://www.nowinkitransferowe.pl/4321"))
        assertNull(EditorsNotificationLinks.articleIdFromUrl("https://nowinkitransferowe.pl/plotki"))
        assertNull(EditorsNotificationLinks.articleIdFromUrl("https://example.com/4321/x"))
        assertNull(EditorsNotificationLinks.articleIdFromUrl(""))
    }

    @Test
    fun rumourOnThePhone_opensItsCard_withoutASync() = runTest {
        rumours.sendRumours(listOf(rumour))
        articles.sendArticles(emptyList())
        val sync = TestSyncManager()

        val link = EditorsNotificationLinks(rumours, transfers, articles, sync).appLink(EditorsPush(articleUrl, rumourId = rumour.id))

        assertEquals("https://nowinkitransferowe.pl/rumour/${rumour.id}", link)
        assertEquals(0, sync.requests)
    }

    @Test
    fun rumourNotYetOnThePhone_opensItsCardAfterTheSync() = runTest {
        rumours.sendRumours(emptyList())
        articles.sendArticles(emptyList())
        val sync = TestSyncManager { rumours.sendRumours(listOf(rumour)) }

        val link = EditorsNotificationLinks(rumours, transfers, articles, sync).appLink(EditorsPush(articleUrl, rumourId = rumour.id))

        assertEquals("https://nowinkitransferowe.pl/rumour/${rumour.id}", link)
        assertEquals(1, sync.requests)
    }

    @Test
    fun withoutTheRumour_theArticleOpensInTheApp() = runTest {
        rumours.sendRumours(emptyList())
        articles.sendArticles(listOf(article))

        val link = EditorsNotificationLinks(rumours, transfers, articles, TestSyncManager()).appLink(EditorsPush(articleUrl))

        assertEquals("https://nowinkitransferowe.pl/news/${article.id}", link)
    }

    @Test
    fun nothingComes_orTheUrlIsNoArticle_opensTheUrl() = runTest {
        rumours.sendRumours(emptyList())
        articles.sendArticles(emptyList())
        val links = EditorsNotificationLinks(rumours, transfers, articles, TestSyncManager())

        assertNull(links.appLink(EditorsPush(articleUrl, rumourId = rumour.id), wait = 1.seconds))
        assertNull(links.appLink(EditorsPush("https://nowinkitransferowe.pl/ligi")))
    }

    @Test
    fun transferPush_opensTheTransferCard_andArticleIdBeatsTheUrl() = runTest {
        rumours.sendRumours(emptyList())
        articles.sendArticles(listOf(article))
        transfers.sendTransfers(transfersTestData.take(1))
        val links = EditorsNotificationLinks(rumours, transfers, articles, TestSyncManager())

        assertEquals(
            "https://nowinkitransferowe.pl/transfer/${transfersTestData[0].id}",
            links.appLink(EditorsPush(articleUrl, transferId = transfersTestData[0].id)),
        )
        assertEquals(
            "https://nowinkitransferowe.pl/news/${article.id}",
            links.appLink(EditorsPush("https://nowinkitransferowe.pl/plotki", articleId = article.id)),
        )
    }
}
