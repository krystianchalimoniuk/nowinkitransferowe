package pl.nowinkitransferowe.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.TransferResourceFtsEntity

/**
 * DAO for [TransferResourceFtsEntity] access.
 */
@Dao
interface TransferResourceFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transferResources: List<TransferResourceFtsEntity>)

    @Query("SELECT transferResourceId FROM transferResourcesFts WHERE transferResourcesFts MATCH :query")
    fun searchAllTransferResources(query: String): Flow<List<String>>

    @Query("SELECT count(*) FROM transferResourcesFts")
    fun getCount(): Flow<Int>
}
