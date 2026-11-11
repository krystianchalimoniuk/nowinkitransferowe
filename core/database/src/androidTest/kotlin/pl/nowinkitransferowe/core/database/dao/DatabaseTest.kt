/*
 * Copyright 2024 The Android Open Source Project
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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import pl.nowinkitransferowe.core.database.NtDatabase

internal abstract class DatabaseTest {

    protected lateinit var db: NtDatabase
    protected lateinit var newsResourceDao: NewsResourceDao
    protected lateinit var transferResourceDao: TransferResourceDao
    protected lateinit var articleDao: ArticleDao
    protected lateinit var transferDao: TransferDao
    protected lateinit var clubDao: ClubDao
    protected lateinit var playerDao: PlayerDao
    protected lateinit var rumourDao: RumourDao
    protected lateinit var statsDao: StatsDao
    protected lateinit var searchDao: SearchDao

    @Before
    fun setup() {
        db = run {
            val context = ApplicationProvider.getApplicationContext<Context>()
            Room.inMemoryDatabaseBuilder(
                context,
                NtDatabase::class.java,
            ).build()
        }
        newsResourceDao = db.newsResourceDao()
        transferResourceDao = db.transferResourceDao()
        articleDao = db.articleDao()
        transferDao = db.transferDao()
        clubDao = db.clubDao()
        playerDao = db.playerDao()
        rumourDao = db.rumourDao()
        statsDao = db.statsDao()
        searchDao = db.searchDao()
    }

    @After
    fun teardown() = db.close()
}
