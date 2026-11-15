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

package pl.nowinkitransferowe.core.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.NewsChannel
import pl.nowinkitransferowe.core.model.NewsGroup
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.model.NewsSubject
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Posts the news notifications on a device and checks what the system shows: a single transfer
 * with its picture, a rumour with its stage, and several news about a club as one notification.
 */
class SystemTrayNotifierTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val notifier = SystemTrayNotifier(context)

    @Before
    fun grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        manager.cancelAll()
        // The system cancels and posts on its own thread, so the tests wait for what they expect
        awaitNotifications { it.isEmpty() }
    }

    /** The active notifications once [condition] holds, failing after a few seconds. */
    private fun awaitNotifications(condition: (List<StatusBarNotification>) -> Boolean): List<StatusBarNotification> {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MILLIS
        while (true) {
            val active = manager.activeNotifications.filter { it.packageName == context.packageName }
            if (condition(active)) return active
            check(SystemClock.uptimeMillis() < deadline) { "Notifications not as expected: ${active.map { it.id }}" }
            SystemClock.sleep(POLL_MILLIS)
        }
    }

    @Test
    fun news_areShownInTheirChannels_andGroupedByClub() {
        val westHam = NewsSubject.Club(83, "West Ham United", null)
        val transfer = transfersTestData[1].copy(to = ClubRef.Club(4, "Liverpool FC"))
        val rumour = rumoursTestData[0].copy(stage = RumourStage.AGREEMENT, previousStage = RumourStage.CLOSE)
        notifier.postNews(
            listOf(
                NewsGroup(NewsSubject.Club(4, "Liverpool FC", null), NewsChannel.FOLLOWED, listOf(NewsItem.TransferNews(transfer))),
                NewsGroup(NewsSubject.Player(3001, "Gabriel Martinelli", null), NewsChannel.FOLLOWED, listOf(NewsItem.RumourNews(rumour))),
                NewsGroup(
                    westHam,
                    NewsChannel.FOLLOWED,
                    listOf(NewsItem.TransferNews(transfersTestData[0]), NewsItem.ArticleNews(articlesTestData[0]), NewsItem.RumourNews(rumoursTestData[1])),
                ),
                NewsGroup(NewsSubject.League("la-liga", "La Liga"), NewsChannel.LEAGUES, listOf(NewsItem.ArticleNews(articlesTestData[1]))),
            ),
        )

        val ids = listOf(NewsItem.TransferNews(transfer).key.hashCode(), "club:83".hashCode(), NewsItem.ArticleNews(articlesTestData[1]).key.hashCode())
        val shown = awaitNotifications { active -> active.map { it.id }.containsAll(ids) }.associateBy { it.id }
        val single = shown.getValue(NewsItem.TransferNews(transfer).key.hashCode()).notification
        assertEquals("followed_channel", single.channelId)
        assertEquals("Oficjalnie: ${transfer.player.name}", single.extras.getString("android.title"))
        assertTrue(single.extras.containsKey("android.picture") || single.extras.containsKey("android.pictureIcon"))

        val grouped = shown.getValue("club:83".hashCode()).notification
        assertEquals("West Ham United", grouped.extras.getString("android.title"))
        assertEquals(3, grouped.extras.getCharSequenceArray("android.textLines")?.size)

        val league = shown.getValue(NewsItem.ArticleNews(articlesTestData[1]).key.hashCode()).notification
        assertEquals("leagues_channel", league.channelId)
    }

    @Test
    fun heldNews_comeAsOneSummary() {
        notifier.postHeldNews(
            listOf(NewsGroup(NewsSubject.Club(83, "West Ham United", null), NewsChannel.FOLLOWED, listOf(NewsItem.TransferNews(transfersTestData[0])))),
        )

        val summary = awaitNotifications { it.isNotEmpty() }.single().notification
        assertEquals("Przez noc: 1 nowość", summary.extras.getString("android.title"))
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val POLL_MILLIS = 50L
    }
}
