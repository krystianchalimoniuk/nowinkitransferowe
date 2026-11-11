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
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ClubAndPlayerDaoTest : DatabaseTest() {

    @Test
    fun a_stub_never_overwrites_a_synced_club() = runTest {
        clubDao.upsertClubs(listOf(club(86, "Liverpool FC")))

        clubDao.insertOrIgnoreClubs(listOf(clubStub(86, "Liverpool")))

        val stored = clubDao.observeClub(86).first()!!
        assertEquals("Liverpool FC", stored.name)
        assertEquals("liverpool-fc", stored.slug)
    }

    @Test
    fun the_synced_club_replaces_its_stub() = runTest {
        clubDao.insertOrIgnoreClubs(listOf(clubStub(86, "Liverpool")))

        clubDao.upsertClubs(listOf(club(86, "Liverpool FC")))

        assertEquals("liverpool-fc", clubDao.observeClub(86).first()!!.slug)
    }

    @Test
    fun upserting_a_club_keeps_everything_that_refers_to_it() = runTest {
        clubDao.upsertClubs(listOf(club(44, "AS Saint-Etienne"), club(86, "Liverpool FC")))
        playerDao.upsertPlayers(listOf(player(10281, "Djylian N'Guessan")))
        articleDao.upsertArticles(listOf(article(11276)), emptyList(), listOf(ArticleClubEntity(11276, 86, 0)), emptyList(), emptyList())
        transferDao.upsertTransfers(listOf(transfer(20568)))

        // A REPLACE would delete the row first and the foreign keys would clear or drop these.
        clubDao.upsertClubs(listOf(club(86, "Liverpool FC (nowa nazwa)")))

        assertEquals(listOf(86), articleDao.observeArticle(11276).first()!!.clubs.map { it.clubId })
        val stored = transferDao.observeTransfer(20568).first()!!
        assertEquals(86, stored.transfer.toClubId)
        assertEquals("Liverpool FC (nowa nazwa)", stored.currentToClubName)
    }

    @Test
    fun lists_and_slug_lookups_skip_stubs() = runTest {
        clubDao.upsertClubs(listOf(club(86, "Liverpool FC")))
        clubDao.insertOrIgnoreClubs(listOf(clubStub(44, "AS Saint-Etienne")))
        playerDao.insertOrIgnorePlayers(listOf(playerStub(10281, "Djylian N'Guessan")))

        assertEquals(listOf(86), clubDao.observeClubs(setOf(44, 86)).first().map { it.id })
        assertEquals(listOf(86), clubDao.getSyncedIds(setOf(44, 86)))
        assertEquals(emptyList(), playerDao.observePlayers(setOf(10281)).first())
        assertNull(clubDao.getClubBySlug("as-saint-etienne"))
    }
}
