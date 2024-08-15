package pl.nowinkitransferowe.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity

/**
 * DAO for [TransferResource] and [TransferResourceEntity] access
 */
@Dao
interface TransferResourceDao {

    /**
     * Fetches transfer resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT * FROM transfer_resources
            WHERE 
                CASE WHEN :useFilterTransferIds
                    THEN id IN (:filterTransferIds)
                    ELSE 1
                END
            ORDER BY CAST(id as INT) DESC
    """,
    )
    fun getTransferResources(
        useFilterTransferIds: Boolean = false,
        filterTransferIds: Set<String> = emptySet(),
    ): Flow<List<TransferResourceEntity>>


    /**
     * Fetches news resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT * FROM transfer_resources ORDER BY CAST(id as INT) DESC LIMIT :limit OFFSET :offset 
    """,
    )
    fun getTransferResourcesPages(limit: Int, offset: Int): Flow<List<TransferResourceEntity>>

    /**
     * Fetches ids of news resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT id FROM transfer_resources
            WHERE 
                CASE WHEN :useFilterTransferIds
                    THEN id IN (:filterTransferIds)
                    ELSE 1
                END
            ORDER BY CAST(id as INT) DESC
    """,
    )
    fun getTransferResourceIds(
        useFilterTransferIds: Boolean = false,
        filterTransferIds: Set<String> = emptySet(),
    ): Flow<List<String>>

    /**
     * Inserts or updates [transferResourceEntities] in the db under the specified primary keys
     */
    @Upsert
    suspend fun upsertTransferResources(transferResourceEntities: List<TransferResourceEntity>)

    /**
     * Deletes rows in the db matching the specified [ids]
     */
    @Query(
        value = """
            DELETE FROM transfer_resources
            WHERE id in (:ids)
        """,
    )
    suspend fun deleteTransferResources(ids: List<String>)

    @Query("SELECT count(*) FROM transfer_resources")
    fun getCount(): Flow<Int>
}
