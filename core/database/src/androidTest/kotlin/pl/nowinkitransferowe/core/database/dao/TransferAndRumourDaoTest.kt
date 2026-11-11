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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test
import pl.nowinkitransferowe.core.database.model.RumourStageEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.RumourStage
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TransferAndRumourDaoTest : DatabaseTest() {

    @Test
    fun a_transfer_shows_the_current_club_and_player_names() = runTest {
        // Stubs carry the copies from the transfer until the clubs and the player are synced.
        clubDao.insertOrIgnoreClubs(listOf(clubStub(44, "AS Saint-Etienne"), clubStub(86, "Liverpool FC")))
        playerDao.insertOrIgnorePlayers(listOf(playerStub(10281, "Djylian N'Guessan")))
        transferDao.upsertTransfers(listOf(transfer(20568)))

        assertEquals("Liverpool FC", transferDao.observeTransfer(20568).first()!!.asExternalModel().to.name)

        clubDao.upsertClubs(listOf(club(86, "Liverpool")))
        playerDao.upsertPlayers(listOf(player(10281, "Djylian N'Guessan Jr")))

        val model = transferDao.observeTransfer(20568).first()!!.asExternalModel()
        assertEquals(ClubRef.Club(86, "Liverpool", "https://nowinkitransferowe.pl/media/w/160/liverpool.png.webp"), model.to)
        assertEquals("Djylian N'Guessan Jr", model.player.name)
    }

    @Test
    fun deleting_a_club_or_article_clears_the_transfer_reference() = runTest {
        clubDao.upsertClubs(listOf(club(44, "AS Saint-Etienne"), club(86, "Liverpool FC")))
        playerDao.upsertPlayers(listOf(player(10281, "Djylian N'Guessan")))
        articleDao.upsertArticles(listOf(article(11276)), emptyList(), emptyList(), emptyList(), emptyList())
        transferDao.upsertTransfers(listOf(transfer(20568, articleId = 11276)))

        clubDao.deleteClubs(listOf(44))
        articleDao.deleteArticles(listOf(11276))

        val stored = transferDao.observeTransfer(20568).first()!!.transfer
        assertNull(stored.fromClubId)
        assertNull(stored.articleId)
        assertEquals(86, stored.toClubId)
    }

    @Test
    fun transfers_filter_by_season_newest_first() = runTest {
        clubDao.insertOrIgnoreClubs(listOf(clubStub(44, "AS Saint-Etienne"), clubStub(86, "Liverpool FC")))
        playerDao.insertOrIgnorePlayers(listOf(playerStub(10281, "Djylian N'Guessan")))
        transferDao.upsertTransfers(
            listOf(
                transfer(1, date = LocalDate(2026, 3, 31)),
                transfer(2, date = LocalDate(2026, 4, 1)),
                transfer(3, date = LocalDate(2027, 1, 2)),
            ),
        )

        assertEquals(listOf(3, 2), transferDao.observeTransfers(season = 2026).first().map { it.transfer.id })
        assertEquals(listOf(3, 2, 1), transferDao.observeTransfers().first().map { it.transfer.id })
        assertEquals(listOf(3, 2, 1), transferDao.observeTransfersOfClub(86).first().map { it.transfer.id })
    }

    @Test
    fun a_rumour_keeps_its_history_in_order_and_replaces_it() = runTest {
        saveRumourDependencies()
        rumourDao.upsertRumours(listOf(rumour(77)), stages(77, 2, 3))

        rumourDao.upsertRumours(listOf(rumour(77)), stages(77, 2, 3, 4))

        val history = rumourDao.observeRumour(77).first()!!.asExternalModel().history
        assertEquals(listOf(RumourStage.INTEREST, RumourStage.TALKS, RumourStage.CLOSE), history.map { it.stage })
    }

    @Test
    fun deleting_a_transfer_or_article_clears_rumour_references() = runTest {
        saveRumourDependencies()
        articleDao.upsertArticles(listOf(article(4321)), emptyList(), emptyList(), emptyList(), emptyList())
        playerDao.upsertPlayers(listOf(player(10281, "Djylian N'Guessan")))
        clubDao.upsertClubs(listOf(club(44, "AS Saint-Etienne"), club(86, "Liverpool FC")))
        transferDao.upsertTransfers(listOf(transfer(20568)))
        rumourDao.upsertRumours(
            listOf(rumour(77, status = "CONFIRMED", transferId = 20568, latestArticleId = 4321)),
            listOf(RumourStageEntity(77, 0, 4, Instant.parse("2026-10-20T10:00:00Z"), articleId = 4321)),
        )

        transferDao.deleteTransfers(listOf(20568))
        articleDao.deleteArticles(listOf(4321))

        val stored = rumourDao.observeRumour(77).first()!!
        assertNull(stored.rumour.transferId)
        assertNull(stored.rumour.latestArticleId)
        assertNull(stored.stages.single().articleId)
    }

    @Test
    fun a_rumour_goes_away_with_its_destination_club() = runTest {
        saveRumourDependencies()
        rumourDao.upsertRumours(listOf(rumour(77)), stages(77, 4))

        clubDao.deleteClubs(listOf(5120))

        assertNull(rumourDao.observeRumour(77).first())
    }

    @Test
    fun the_board_shows_active_rumours_updated_since_a_date() = runTest {
        saveRumourDependencies()
        rumourDao.upsertRumours(
            listOf(
                rumour(1, updatedAt = "2026-10-20T10:00:00Z"),
                rumour(2, updatedAt = "2026-08-01T10:00:00Z"),
                rumour(3, status = "FAILED", updatedAt = "2026-10-25T10:00:00Z"),
                rumour(4, updatedAt = "2026-10-28T10:00:00Z"),
            ),
            emptyList(),
        )

        val board = rumourDao.observeRumours("ACTIVE", Instant.parse("2026-09-15T00:00:00Z")).first()

        assertEquals(listOf(4, 1), board.map { it.rumour.id })
    }

    private suspend fun saveRumourDependencies() {
        playerDao.insertOrIgnorePlayers(listOf(playerStub(12148, "Gabriel Martinelli")))
        clubDao.insertOrIgnoreClubs(listOf(clubStub(37, "Arsenal FC"), clubStub(5120, "Al-Hilal SFC")))
    }

    private fun stages(rumourId: Int, vararg levels: Int) = levels.mapIndexed { position, level ->
        RumourStageEntity(rumourId, position, level, Instant.parse("2026-10-0${position + 1}T08:00:00Z"), articleId = null)
    }
}
