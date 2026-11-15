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

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.notifications.articleAppLink
import pl.nowinkitransferowe.core.notifications.rumourAppLink
import pl.nowinkitransferowe.core.notifications.transferAppLink
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** What an editors' push points to: a rumour, a transfer or an article, and the url to open otherwise. */
data class EditorsPush(
    val url: String,
    val rumourId: Int? = null,
    val transferId: Int? = null,
    val articleId: Int? = null,
)

/**
 * Where an editors' push notification leads in the app: the rumour card for a push with a
 * rumour_id, the transfer card for one with a transfer_id, otherwise the article (its article_id,
 * or the id in its url).
 *
 * The push often comes before the sync that brings the record, so when none of them is on the
 * phone yet, a sync is requested and awaited for a while. Without them the url opens as before.
 */
class EditorsNotificationLinks @Inject constructor(
    private val rumourRepository: RumourRepository,
    private val transferRepository: TransferRepository,
    private val articleRepository: ArticleRepository,
    private val syncManager: SyncManager,
) {

    /** The in-app link for [push], or null to open its url itself. */
    suspend fun appLink(push: EditorsPush, wait: Duration = SYNC_WAIT): String? {
        val articleId = push.articleId ?: articleIdFromUrl(push.url)
        val records = listOfNotNull(
            push.rumourId?.let { id -> rumourRepository.observeRumour(id).map { it != null } to rumourAppLink(id) },
            push.transferId?.let { id -> transferRepository.observeTransfer(id).map { it != null } to transferAppLink(id) },
            articleId?.let { id -> articleRepository.observeArticle(id).map { it != null } to articleAppLink(id) },
        )
        if (records.isEmpty()) return null
        // The first record that is on the phone, in the order above
        suspend fun link(): String? = records.firstOrNull { (isHere, _) -> isHere.first() }?.second
        link()?.let { return it }

        syncManager.requestSync()
        withTimeoutOrNull(wait) {
            combine(records.map { it.first }) { here -> here.any { it } }.first { it }
        }
        return link()
    }

    companion object {
        private val SYNC_WAIT = 30.seconds
        private val ARTICLE_URL = Regex("""^https?://(www\.)?nowinkitransferowe\.pl/(\d+)(/.*)?$""")

        /** The id of the article at [url], like 4321 in https://nowinkitransferowe.pl/4321/transfery/slug. */
        fun articleIdFromUrl(url: String): Int? = ARTICLE_URL.matchEntire(url.trim())?.groupValues?.get(2)?.toIntOrNull()
    }
}
