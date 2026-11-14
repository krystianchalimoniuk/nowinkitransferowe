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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.model.clubEntities
import pl.nowinkitransferowe.core.data.model.clubStub
import pl.nowinkitransferowe.core.data.model.playerEntities
import pl.nowinkitransferowe.core.data.model.playerStub
import pl.nowinkitransferowe.core.data.model.tagEntities
import pl.nowinkitransferowe.core.data.model.topicEntities
import pl.nowinkitransferowe.core.data.syncChanges
import pl.nowinkitransferowe.core.database.dao.ArticleDao
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.dao.PlayerDao
import pl.nowinkitransferowe.core.database.model.PopulatedArticle
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.notifications.Notifier
import javax.inject.Inject

internal class OfflineFirstArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
    private val network: NtNetworkDataSource,
    private val ntPreferencesDataSource: NtPreferencesDataSource,
    private val notifier: Notifier,
) : ArticleRepository {

    override fun observeArticle(id: Int): Flow<Article?> = articleDao.observeArticle(id).map { it?.asExternalModel() }

    override fun observeArticles(kinds: Set<ArticleKind>?, topic: String?, limit: Int): Flow<List<Article>> = articleDao.observeArticles(
        filterKinds = kinds != null,
        kinds = kinds.orEmpty().mapTo(mutableSetOf(), ArticleKind::name),
        topic = topic,
        limit = limit,
    ).map { it.map(PopulatedArticle::asExternalModel) }

    override fun observeArticles(ids: Set<Int>): Flow<List<Article>> = articleDao.observeArticlesByIds(ids).map { it.map(PopulatedArticle::asExternalModel) }

    override fun observeArticleCount(): Flow<Int> = articleDao.observeArticleCount()

    override fun observeArticleIdsAfter(id: Int): Flow<List<Int>> = articleDao.observeIdsAfter(id)

    override suspend fun getNewestArticleId(): Int? = articleDao.getNewestId()

    override fun observeArticlesAboutClub(clubId: Int): Flow<List<Article>> = articleDao.observeArticlesAboutClub(clubId).map { it.map(PopulatedArticle::asExternalModel) }

    override fun observeArticlesAboutPlayer(playerId: Int): Flow<List<Article>> = articleDao.observeArticlesAboutPlayer(playerId).map { it.map(PopulatedArticle::asExternalModel) }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        // Nothing is announced on the first sync, which downloads every article.
        val isFirstSync = synchronizer.getChangeListVersions().articleVersion == 0
        val addedIds = mutableSetOf<Int>()
        val synced = synchronizer.syncChanges(
            versionReader = ChangeListVersions::articleVersion,
            changesFetcher = { after -> network.getChanges(NetworkRecordType.ARTICLES, after) },
            versionUpdater = { copy(articleVersion = it) },
            modelDeleter = articleDao::deleteArticles,
            modelUpdater = { ids ->
                val articles = network.getArticles(ids)
                if (!isFirstSync) {
                    val existingIds = articleDao.getExistingIds(ids.toSet()).toSet()
                    addedIds += articles.map { it.id }.filter { it !in existingIds }
                }
                // Articles carry only the ids of their clubs and players.
                clubDao.insertOrIgnoreClubs(articles.flatMap { it.clubIds }.distinct().map { clubStub(it, null) })
                playerDao.insertOrIgnorePlayers(articles.flatMap { it.playerIds }.distinct().map { playerStub(it, null) })
                articleDao.upsertArticles(
                    articles = articles.map { it.asEntity() },
                    topics = articles.flatMap { it.topicEntities() },
                    clubs = articles.flatMap { it.clubEntities() },
                    players = articles.flatMap { it.playerEntities() },
                    tags = articles.flatMap { it.tagEntities() },
                )
            },
        )
        // After the first sync everything there is counts as read; later articles are new
        if (synced) ntPreferencesDataSource.setReadUpToIfUnset(articleId = articleDao.getNewestId(), transferId = null)
        if (synced && addedIds.isNotEmpty() && ntPreferencesDataSource.userData.first().isNewsNotificationsAllowed) {
            notifier.postArticleNotifications(observeArticles(addedIds).first())
        }
        return synced
    }
}
