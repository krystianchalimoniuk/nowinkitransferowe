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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.suspendRunCatching
import pl.nowinkitransferowe.core.database.dao.DictionaryDao
import pl.nowinkitransferowe.core.database.model.CountryEntity
import pl.nowinkitransferowe.core.database.model.TopicEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject

/**
 * Topics and countries change only when the website is deployed and the API has no change list for them,
 * so they are downloaded whole on the first sync and again only when the records use a code they lack.
 */
internal class OfflineFirstDictionaryRepository @Inject constructor(
    private val dictionaryDao: DictionaryDao,
    private val network: NtNetworkDataSource,
) : DictionaryRepository {

    override fun observeTopics(): Flow<List<Topic>> = dictionaryDao.observeTopics().map { it.map(TopicEntity::asExternalModel) }

    override fun observeCountries(): Flow<List<Country>> = dictionaryDao.observeCountries().map { it.map(CountryEntity::asExternalModel) }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = suspendRunCatching {
        if (!dictionaryDao.hasMissingEntries()) return@suspendRunCatching

        val topics = network.getTopics()
        val countries = network.getCountries()
        dictionaryDao.replaceTopics(topics.map { it.asEntity() })
        dictionaryDao.replaceCountries(countries.map { it.asEntity() })
    }.isSuccess
}
