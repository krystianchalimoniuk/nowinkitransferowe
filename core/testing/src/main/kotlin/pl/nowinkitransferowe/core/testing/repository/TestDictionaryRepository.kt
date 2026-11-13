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

package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.testing.data.topicsTestData

class TestDictionaryRepository : DictionaryRepository {

    private val topics = MutableStateFlow(topicsTestData)

    private val countries = MutableStateFlow(emptyList<Country>())

    override fun observeTopics(): Flow<List<Topic>> = topics

    override fun observeCountries(): Flow<List<Country>> = countries

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    fun sendTopics(topics: List<Topic>) {
        this.topics.value = topics
    }

    fun sendCountries(countries: List<Country>) {
        this.countries.value = countries
    }
}
