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

package pl.nowinkitransferowe.core.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceFtsDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceFtsDao
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.database.model.asFtsEntity
import pl.nowinkitransferowe.core.model.SearchResult
import javax.inject.Inject

internal class DefaultSearchContentsRepository @Inject constructor(
    private val newsResourceDao: NewsResourceDao,
    private val newsResourceFtsDao: NewsResourceFtsDao,
    private val transferResourceDao: TransferResourceDao,
    private val transferResourceFtsDao: TransferResourceFtsDao,
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : SearchContentsRepository {

    override suspend fun populateFtsData() {
        withContext(ioDispatcher) {
            newsResourceFtsDao.insertAll(
                newsResourceDao.getNewsResources(
                    useFilterNewsIds = false,
                )
                    .first()
                    .map(NewsResourceEntity::asFtsEntity),
            )
            transferResourceFtsDao.insertAll(
                transferResourceDao.getTransferResources(useFilterTransferIds = false)
                    .first()
                    .map { it.asFtsEntity() },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun searchContents(searchQuery: String): Flow<SearchResult> {
        // Surround the query by asterisks to match the query when it's in the middle of
        // a word
        val newsResourceIds = newsResourceFtsDao.searchAllNewsResources("*$searchQuery*")
        val transferResourceIds =
            transferResourceFtsDao.searchAllTransferResources("*$searchQuery*")

        val newsResourcesFlow = newsResourceIds
            .mapLatest { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest {
                newsResourceDao.getNewsResources(useFilterNewsIds = true, filterNewsIds = it)
            }
        val transferResourcesFlow = transferResourceIds
            .mapLatest { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest {
                transferResourceDao.getTransferResources(
                    useFilterTransferIds = true,
                    filterTransferIds = it,
                )
            }
        return combine(
            newsResourcesFlow,
            transferResourcesFlow,
        ) { newsResources, transferResources ->
            SearchResult(
                transferResources = transferResources.map { it.asExternalModel() }.sortedByDescending { it.id.toInt() },
                newsResources = newsResources.map { it.asExternalModel() },
            )
        }
    }

    override fun getSearchContentsCount(): Flow<Int> =
        combine(
            newsResourceFtsDao.getCount(),
            transferResourceFtsDao.getCount(),
        ) { newsResourceCount, topicsCount ->
            newsResourceCount + topicsCount
        }
}
