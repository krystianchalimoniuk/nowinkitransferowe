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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.TransferResourceFtsEntity

/**
 * DAO for [TransferResourceFtsEntity] access.
 */
@Dao
interface TransferResourceFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transferResources: List<TransferResourceFtsEntity>)

    @Query("SELECT transferResourceId FROM transferResourcesFts WHERE transferResourcesFts MATCH :query")
    fun searchAllTransferResources(query: String): Flow<List<String>>

    @Query("SELECT count(*) FROM transferResourcesFts")
    fun getCount(): Flow<Int>
}
