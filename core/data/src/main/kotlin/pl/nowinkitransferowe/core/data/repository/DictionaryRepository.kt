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
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Topic

/**
 * Topics (leagues, cups, special topics) and country names from API v3. They have no change list,
 * so a sync downloads them again only when they are empty or the records use a code they lack.
 */
interface DictionaryRepository : Syncable {
    fun observeTopics(): Flow<List<Topic>>

    fun observeCountries(): Flow<List<Country>>
}
