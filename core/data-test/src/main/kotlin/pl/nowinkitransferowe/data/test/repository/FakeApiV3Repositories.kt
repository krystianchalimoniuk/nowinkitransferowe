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

package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.Transfer
import javax.inject.Inject

// Empty fakes of the API v3 repositories for the app's Hilt tests. No screen reads them yet;
// they get demo data when the screens move to API v3.

class FakeDictionaryRepository @Inject constructor() : DictionaryRepository {
    override fun observeTopics(): Flow<List<Topic>> = flowOf(emptyList())

    override fun observeCountries(): Flow<List<Country>> = flowOf(emptyList())

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeArticleRepository @Inject constructor() : ArticleRepository {
    override fun observeArticle(id: Int): Flow<Article?> = flowOf(null)

    override fun observeArticles(kinds: Set<ArticleKind>?, topic: String?, limit: Int): Flow<List<Article>> = flowOf(emptyList())

    override fun observeArticles(ids: Set<Int>): Flow<List<Article>> = flowOf(emptyList())

    override fun observeArticlesAboutClub(clubId: Int): Flow<List<Article>> = flowOf(emptyList())

    override fun observeArticlesAboutPlayer(playerId: Int): Flow<List<Article>> = flowOf(emptyList())

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeTransferRepository @Inject constructor() : TransferRepository {
    override fun observeTransfer(id: Int): Flow<Transfer?> = flowOf(null)

    override fun observeTransfers(season: Season?, limit: Int): Flow<List<Transfer>> = flowOf(emptyList())

    override fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>> = flowOf(emptyList())

    override fun observeTransfersOfClub(clubId: Int): Flow<List<Transfer>> = flowOf(emptyList())

    override fun observeTransfersOfPlayer(playerId: Int): Flow<List<Transfer>> = flowOf(emptyList())

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeRumourRepository @Inject constructor() : RumourRepository {
    override fun observeRumour(id: Int): Flow<Rumour?> = flowOf(null)

    override fun observeRumours(status: RumourStatus, updatedSince: Instant): Flow<List<Rumour>> = flowOf(emptyList())

    override fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>> = flowOf(emptyList())

    override fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>> = flowOf(emptyList())

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeClubRepository @Inject constructor() : ClubRepository {
    override fun observeClub(id: Int): Flow<Club?> = flowOf(null)

    override fun observeClubs(ids: Set<Int>): Flow<List<Club>> = flowOf(emptyList())

    override suspend fun getClubBySlug(slug: String): Club? = null

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakePlayerRepository @Inject constructor() : PlayerRepository {
    override fun observePlayer(id: Int): Flow<Player?> = flowOf(null)

    override fun observePlayers(ids: Set<Int>): Flow<List<Player>> = flowOf(emptyList())

    override suspend fun getPlayerBySlug(slug: String): Player? = null

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}
