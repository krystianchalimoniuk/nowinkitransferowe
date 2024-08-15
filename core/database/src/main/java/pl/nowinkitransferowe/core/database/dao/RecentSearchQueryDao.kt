package pl.nowinkitransferowe.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.RecentSearchQueryEntity

@Dao
interface RecentSearchQueryDao {
    @Query(value = "SELECT * FROM recentSearchQueries ORDER BY queriedDate DESC LIMIT :limit")
    fun getRecentSearchQueryEntities(limit: Int): Flow<List<RecentSearchQueryEntity>>

    @Upsert
    suspend fun insertOrReplaceRecentSearchQuery(recentSearchQuery: RecentSearchQueryEntity)

    @Query(value = "DELETE FROM recentSearchQueries")
    suspend fun clearRecentSearchQueries()
}