package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.model.UserTransferResource

interface UserTransferResourceRepository {
    /**
     * Returns available transfers resources as a stream.
     */
    fun observeAll(
        query: TransferResourceQuery = TransferResourceQuery(
            filterTransferIds = null,
        ),
    ): Flow<List<UserTransferResource>>

    /**
     * Returns the user's bookmarked transfer resources as a stream.
     */
    fun observeAllBookmarked(): Flow<List<UserTransferResource>>

    /**
     * Returns available news resources that match the specified [limit] and [offset].
     */
    fun observeAllPages(
        limit: Int, offset: Int,
    ): Flow<List<UserTransferResource>>

    /**
     * Returns counts number.
     */
    fun getCount(): Flow<Int>
}
