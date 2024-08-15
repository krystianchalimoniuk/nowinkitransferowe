package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.model.mapToUserTransferResources
import javax.inject.Inject

class CompositeUserTransferResourceRepository @Inject constructor(
    val transferRepository: TransferRepository,
    val userDataRepository: UserDataRepository,
) : UserTransferResourceRepository {

    /**
     * Returns available news resources (joined with user data) matching the given query.
     */
    override fun observeAll(
        query: TransferResourceQuery,
    ): Flow<List<UserTransferResource>> =
        transferRepository.getTransferResources(query)
            .combine(userDataRepository.userData) { transferResources, userData ->
                transferResources.mapToUserTransferResources(userData)
            }


    override fun observeAllBookmarked(): Flow<List<UserTransferResource>> =
        userDataRepository.userData.map { it.bookmarkedTransferResources }.distinctUntilChanged()
            .flatMapLatest { bookmarkedTransferResources ->
                when {
                    bookmarkedTransferResources.isEmpty() -> flowOf(emptyList())
                    else -> observeAll(TransferResourceQuery(filterTransferIds = bookmarkedTransferResources))
                }
            }

    override fun observeAllPages(
        limit: Int,
        offset: Int,
    ): Flow<List<UserTransferResource>> =
        transferRepository.getTransferResourcesPages(limit, offset)
            .combine(userDataRepository.userData) { transferResources, userData ->
                transferResources.mapToUserTransferResources(userData)
            }

    override fun getCount(): Flow<Int> =
        transferRepository.getCount()

}
