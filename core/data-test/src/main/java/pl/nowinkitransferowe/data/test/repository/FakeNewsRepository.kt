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

package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.NewsResourceQuery
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import javax.inject.Inject

/**
 * Fake implementation of the [NewsRepository] that retrieves the news resources from a JSON String.
 *
 * This allows us to run the app with fake data, without needing an internet connection or working
 * backend.
 */
class FakeNewsRepository @Inject constructor(
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val datasource: DemoNtNetworkDataSource,
) : NewsRepository {

    override fun getNewsResources(
        query: NewsResourceQuery,
    ): Flow<List<NewsResource>> =
        flow {
            emit(
                datasource
                    .getNewsResources()
                    .map(NetworkNewsResource::asEntity)
                    .map(NewsResourceEntity::asExternalModel),
            )
        }.flowOn(ioDispatcher)

    override fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResource>> =
        flow {
            emit(
                datasource.getNewsResources().take(limit).map(NetworkNewsResource::asEntity)
                    .map(NewsResourceEntity::asExternalModel),
            )
        }

    override fun getNewsResource(id: String): Flow<NewsResource> = flow {
        emit(
            datasource.getNewsResources().first { it.id == id.toInt() }.asEntity().asExternalModel(),
        )
    }

    override fun getNewsCount(): Flow<Int> = flow {
        emit(
            datasource.getNewsResources().size,
        )
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}
