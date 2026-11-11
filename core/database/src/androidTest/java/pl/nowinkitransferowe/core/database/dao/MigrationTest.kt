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

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.database.NtDatabase
import kotlin.test.assertEquals

internal class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), NtDatabase::class.java)

    @Test
    fun migrating_from_2_to_3_keeps_the_v2_data_and_adds_the_v3_tables() {
        helper.createDatabase(DB_NAME, 2).use { db ->
            db.execSQL(
                """
                INSERT INTO transfer_resources (id, name, footballer_img, club_from, club_from_img, club_to, club_to_img, price, url, publish_date, season)
                VALUES ('20567', 'Gabriel Martinelli', '', 'Arsenal FC', '', 'Al-Hilal SFC', '', '70 mln €', '', 0, '26/27')
                """,
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 3, true).use { db ->
            db.query("SELECT name FROM transfer_resources WHERE id = '20567'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Gabriel Martinelli", cursor.getString(0))
            }
            db.query("SELECT count(*) FROM transfers").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
