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

package pl.nowinkitransferowe.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.UserSearchResult
import pl.nowinkitransferowe.core.model.mapToUserArticles
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import javax.inject.Inject

/**
 * A use case which returns the searched contents matched with the search query.
 */
class GetSearchContentUseCase @Inject constructor(
    private val searchContentsRepository: SearchContentsRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val userDataRepository: UserDataRepository,
) {

    operator fun invoke(
        searchQuery: String,
    ): Flow<UserSearchResult> = combine(
        searchContentsRepository.searchContents(searchQuery),
        dictionaryRepository.observeTopics(),
        userDataRepository.userData,
    ) { searchResult, topics, userData ->
        UserSearchResult(
            articles = searchResult.articles.mapToUserArticles(topics, userData),
            transfers = searchResult.transfers.mapToUserTransfers(userData),
        )
    }
}
