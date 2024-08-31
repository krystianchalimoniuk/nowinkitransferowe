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

package pl.nowinkitransferowe.core.database


import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Automatic schema migrations sometimes require extra instructions to perform the migration, for
 * example, when a column is renamed. These extra instructions are placed here by creating a class
 * using the following naming convention `SchemaXtoY` where X is the schema version you're migrating
 * from and Y is the schema version you're migrating to. The class should implement
 * `AutoMigrationSpec`.
 */
internal object DatabaseMigrations {

    class Schema1to2 : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            val date20170702InMillis = 1499004000000L // 2017-07-02 14:00:00
            val date20180103InMillis = 1514988000000L // 2018-01-03 14:00:00
            val date20180704InMillis = 1530712800000L // 2018-07-04 14:00:00
            val date20190105InMillis = 1546696800000L // 2019-01-05 14:00:00
            val date20190706InMillis = 1562414400000L // 2019-07-06 14:00:00
            val date20200107InMillis = 1578405600000L // 2020-01-07 14:00:00
            val date20200707InMillis = 1594123200000L // 2020-07-07 14:00:00
            val date20210108InMillis = 1610114400000L // 2021-01-08 14:00:00
            val date20210709InMillis = 1625832000000L // 2021-07-09 14:00:00
            val date20220110InMillis = 1641823200000L // 2022-01-10 14:00:00
            val date20220711InMillis = 1657540800000L // 2022-07-11 14:00:00
            val date20230112InMillis = 1673532000000L // 2023-01-12 14:00:00
            val date20230716InMillis = 1689519724000L // 2023-07-16 14:22:04
            val date20240115InMillis = 1705328524000L // 2024-01-15 14:22:04
            val date20240830InMillis = 1725028924000L // 2024-08-30 15:22:04

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20170702InMillis, season = '17/18' 
                WHERE id BETWEEN '3' AND '357' AND publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20180103InMillis, season = '17/18' 
                WHERE id BETWEEN '358' AND '811' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20180704InMillis, season = '18/19' 
                WHERE id BETWEEN '812' AND '1531' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20190105InMillis, season = '18/19' 
                WHERE id BETWEEN '1532' AND '2090' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20190706InMillis, season = '19/20' 
                WHERE id BETWEEN '2091' AND '3747' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20200107InMillis, season = '19/20' 
                WHERE id BETWEEN '3748' AND '4299' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20200707InMillis, season = '20/21' 
                WHERE id BETWEEN '4300' AND '5870' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20210108InMillis, season = '20/21' 
                WHERE id BETWEEN '5872' AND '6639' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20210709InMillis, season = '21/22' 
                WHERE id BETWEEN '6640' AND '8269' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20220110InMillis, season = '21/22' 
                WHERE id BETWEEN '8270' AND '8410' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20220711InMillis, season = '22/23' 
                WHERE id BETWEEN '8411' AND '10938' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20230112InMillis, season = '22/23' 
                WHERE id BETWEEN '10939' AND '11843' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20230716InMillis, season = '23/24' 
                WHERE id BETWEEN '11844' AND '13939' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20240115InMillis, season = '23/24' 
                WHERE id BETWEEN '13940' AND '14856' AND (publish_date IS NULL OR season IS NULL)
                """,
            )

            db.execSQL(
                """
                UPDATE transfer_resources
                SET publish_date = $date20240830InMillis, season = '24/25' 
                WHERE id BETWEEN '14857' AND '16931' AND (publish_date IS NULL OR season IS NULL)
                """,
            )
        }
    }
}
