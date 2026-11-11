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

package pl.nowinkitransferowe.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.nowinkitransferowe.core.database.model.ArticleClubEntity
import pl.nowinkitransferowe.core.database.model.ArticleTagEntity
import pl.nowinkitransferowe.core.database.model.ArticleTopicEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import kotlin.test.assertEquals

internal class ArticleAndSearchDaoTest : DatabaseTest() {

    @Test
    fun saving_an_article_again_replaces_its_relations_in_order() = runTest {
        clubDao.insertOrIgnoreClubs(listOf(clubStub(86, ""), clubStub(44, ""), clubStub(726, "")))
        articleDao.upsertArticles(
            listOf(article(11276)),
            listOf(ArticleTopicEntity(11276, "premier-league", 0)),
            listOf(ArticleClubEntity(11276, 86, 0)),
            emptyList(),
            listOf(ArticleTagEntity(11276, "Liverpool FC", 0)),
        )

        articleDao.upsertArticles(
            listOf(article(11276)),
            listOf(ArticleTopicEntity(11276, "premier-league", 0), ArticleTopicEntity(11276, "ligue-1", 1)),
            listOf(ArticleClubEntity(11276, 86, 0), ArticleClubEntity(11276, 44, 1), ArticleClubEntity(11276, 726, 2)),
            emptyList(),
            listOf(ArticleTagEntity(11276, "Djylian N'Guessan", 0), ArticleTagEntity(11276, "Liverpool FC", 1)),
        )

        val model = articleDao.observeArticle(11276).first()!!.asExternalModel()
        assertEquals(listOf("premier-league", "ligue-1"), model.topics)
        assertEquals(listOf(86, 44, 726), model.clubIds)
        assertEquals(listOf("Djylian N'Guessan", "Liverpool FC"), model.tags)
    }

    @Test
    fun articles_filter_by_kind_and_topic_newest_first() = runTest {
        articleDao.upsertArticles(
            listOf(
                article(1, kind = "OFFICIAL", publishedAt = "2026-10-01T10:00:00Z"),
                article(2, kind = "RUMOUR", publishedAt = "2026-10-02T10:00:00Z"),
                article(3, kind = "RUMOUR", publishedAt = "2026-10-03T10:00:00Z"),
            ),
            listOf(ArticleTopicEntity(1, "la-liga", 0), ArticleTopicEntity(3, "la-liga", 0)),
            emptyList(),
            emptyList(),
            emptyList(),
        )

        assertEquals(listOf(3, 2, 1), articleDao.observeArticles().first().map { it.article.id })
        assertEquals(listOf(3, 2), articleDao.observeArticles(filterKinds = true, kinds = setOf("RUMOUR")).first().map { it.article.id })
        assertEquals(listOf(3, 1), articleDao.observeArticles(topic = "la-liga").first().map { it.article.id })
        assertEquals(listOf(3), articleDao.observeArticles(topic = "la-liga", limit = 1).first().map { it.article.id })
        assertEquals(listOf(1, 3), articleDao.getExistingIds(setOf(1, 3, 99)).sorted())
    }

    @Test
    fun deleting_a_club_removes_it_from_articles() = runTest {
        clubDao.insertOrIgnoreClubs(listOf(clubStub(86, ""), clubStub(44, "")))
        articleDao.upsertArticles(
            listOf(article(11276)),
            emptyList(),
            listOf(ArticleClubEntity(11276, 86, 0), ArticleClubEntity(11276, 44, 1)),
            emptyList(),
            emptyList(),
        )

        clubDao.deleteClubs(listOf(44))

        assertEquals(listOf(86), articleDao.observeArticle(11276).first()!!.asExternalModel().clubIds)
        assertEquals(listOf(11276), articleDao.observeArticlesAboutClub(86).first().map { it.article.id })
    }

    @Test
    fun search_follows_inserts_updates_and_deletes() = runTest {
        clubDao.upsertClubs(listOf(club(86, "Liverpool FC")))
        clubDao.insertOrIgnoreClubs(listOf(clubStub(87, "Liverpool Reserves")))
        playerDao.insertOrIgnorePlayers(listOf(playerStub(10281, "Djylian N'Guessan")))
        clubDao.insertOrIgnoreClubs(listOf(clubStub(44, "AS Saint-Etienne")))
        transferDao.upsertTransfers(listOf(transfer(20568)))

        // Stubs are indexed but left out of club results.
        assertEquals(listOf(86), searchDao.searchClubIds("liverpool*").first())
        assertEquals(listOf(20568), searchDao.searchTransferIds("liverpool*").first())

        clubDao.upsertClubs(listOf(club(86, "Liverpool")))
        assertEquals(listOf(86), searchDao.searchClubIds("liverpool").first())
        assertEquals(emptyList(), searchDao.searchClubIds("fc").first())

        clubDao.deleteClubs(listOf(86))
        assertEquals(emptyList(), searchDao.searchClubIds("liverpool*").first())
    }

    @Test
    fun search_finds_articles_by_title_and_lead() = runTest {
        articleDao.upsertArticles(listOf(article(11276)), emptyList(), emptyList(), emptyList(), emptyList())

        assertEquals(listOf(11276), searchDao.searchArticleIds("artykuł").first())
        assertEquals(listOf(11276), searchDao.searchArticleIds("lead").first())
    }
}
