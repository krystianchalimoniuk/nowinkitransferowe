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

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserDataTest {
    private val userData = UserData(
        bookmarkedArticles = emptySet(),
        viewedArticles = setOf(105),
        bookmarkedTransfers = emptySet(),
        viewedTransfers = setOf(205),
        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = true,
        isGeneralNotificationAllowed = true,
        articlesReadUpToId = 100,
        transfersReadUpToId = 200,
    )

    @Test
    fun recordsUpToTheMarkerOrOpenedAreRead() {
        assertTrue(userData.isTransferRead(150))
        assertTrue(userData.isTransferRead(200))
        assertTrue(userData.isTransferRead(205))
        assertFalse(userData.isTransferRead(201))
        assertTrue(userData.isArticleRead(105))
        assertFalse(userData.isArticleRead(101))
    }

    @Test
    fun beforeTheFirstSyncEverythingIsRead() {
        val fresh = userData.copy(articlesReadUpToId = 0, transfersReadUpToId = 0)
        assertTrue(fresh.isTransferRead(10_000))
        assertTrue(fresh.isArticleRead(10_000))
    }
}
