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

import pl.nowinkitransferowe.core.model.RumourStage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What one sync brought that was not on the phone before: new articles and transfers, and
 * rumours that are new or moved to a higher stage, with that stage. Edits of records already on
 * the phone and stages going down are not news.
 */
data class SyncNews(
    val articleIds: Set<Int> = emptySet(),
    val transferIds: Set<Int> = emptySet(),
    val rumourStages: Map<Int, RumourStage> = emptyMap(),
) {
    fun isEmpty() = articleIds.isEmpty() && transferIds.isEmpty() && rumourStages.isEmpty()
}

/**
 * Collects [SyncNews] while the repositories save a sync, for the notifications that follow it.
 * The first sync of a record type, which downloads everything, records nothing.
 */
@Singleton
class SyncNewsRecorder @Inject constructor() {
    private var news = SyncNews()

    @Synchronized
    fun recordArticles(ids: Collection<Int>) {
        news = news.copy(articleIds = news.articleIds + ids)
    }

    @Synchronized
    fun recordTransfers(ids: Collection<Int>) {
        news = news.copy(transferIds = news.transferIds + ids)
    }

    @Synchronized
    fun recordRumourStages(stages: Map<Int, RumourStage>) {
        news = news.copy(rumourStages = news.rumourStages + stages)
    }

    /** Returns what was recorded since the last call and starts over. */
    @Synchronized
    fun take(): SyncNews = news.also { news = SyncNews() }
}
