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

package pl.nowinkitransferowe.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.SearchResult
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.model.UserSearchResult
import pl.nowinkitransferowe.core.model.UserTransferResource
import javax.inject.Inject

/**
 * A use case which returns the searched contents matched with the search query.
 */
class GetSearchContentUseCase @Inject constructor(
    private val searchContentsRepository: SearchContentsRepository,
    private val userDataRepository: UserDataRepository,
) {

    operator fun invoke(
        searchQuery: String,
    ): Flow<UserSearchResult> =
        searchContentsRepository.searchContents(searchQuery)
            .mapToUserSearchResult(userDataRepository.userData)
}

private fun Flow<SearchResult>.mapToUserSearchResult(userDataStream: Flow<UserData>): Flow<UserSearchResult> =
    combine(userDataStream) { searchResult, userData ->
        UserSearchResult(
            transferResources = searchResult.transferResources.map { transfer ->
                UserTransferResource(
                    transferResource = transfer,
                    userData = userData,
                )
            },
            newsResources = searchResult.newsResources.map { news ->
                UserNewsResource(
                    newsResource = news,
                    userData = userData,
                )
            },
        )
    }
