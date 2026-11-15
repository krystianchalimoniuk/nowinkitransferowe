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

package pl.nowinkitransferowe.core.model

/** A record the app notifies about after a sync, with the key that keeps it from coming twice. */
sealed interface NewsItem {
    val key: String

    data class TransferNews(val transfer: Transfer) : NewsItem {
        override val key get() = "$TRANSFER_KEY:${transfer.id}"
    }

    /** A rumour that is new or moved up to its current stage; each stage is news once. */
    data class RumourNews(val rumour: Rumour) : NewsItem {
        override val key get() = "$RUMOUR_KEY:${rumour.id}:${rumour.stage.level}"
    }

    data class ArticleNews(val article: Article) : NewsItem {
        override val key get() = "$ARTICLE_KEY:${article.id}"
    }

    companion object {
        const val TRANSFER_KEY = "transfer"
        const val RUMOUR_KEY = "rumour"
        const val ARTICLE_KEY = "article"
    }
}

/** Who the news is about: a followed club or player, or a followed league. */
sealed interface NewsSubject {
    val name: String

    /** "club:83", "player:500" or "league:la-liga": one notification and one mute per subject. */
    val key: String
        get() = when (this) {
            is Club -> "club:$id"
            is Player -> "player:$id"
            is League -> "league:$topic"
        }

    data class Club(val id: Int, override val name: String, val crestUrl: String?) : NewsSubject

    data class Player(val id: Int, override val name: String, val photoUrl: String?) : NewsSubject

    data class League(val topic: String, override val name: String) : NewsSubject
}

/** The notification channel the news goes to. */
enum class NewsChannel { FOLLOWED, LEAGUES }

/** The news about one [subject], newest first; several items make one grouped notification. */
data class NewsGroup(
    val subject: NewsSubject,
    val channel: NewsChannel,
    val items: List<NewsItem>,
)
