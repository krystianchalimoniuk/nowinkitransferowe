package pl.nowinkitransferowe.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceFtsDao
import pl.nowinkitransferowe.core.database.dao.RecentSearchQueryDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceFtsDao
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.NewsResourceFtsEntity
import pl.nowinkitransferowe.core.database.model.RecentSearchQueryEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceFtsEntity
import pl.nowinkitransferowe.core.database.util.InstantConverter

@Database(
    entities = [
        NewsResourceEntity::class,
        NewsResourceFtsEntity::class,
        TransferResourceEntity::class,
        TransferResourceFtsEntity::class,
        RecentSearchQueryEntity::class
    ],
    version = 1,
    autoMigrations = [
    ],
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
)
internal abstract class NtDatabase : RoomDatabase() {
    abstract fun newsResourceDao(): NewsResourceDao
    abstract fun newsResourceFtsDao(): NewsResourceFtsDao
    abstract fun transferResourceDao(): TransferResourceDao
    abstract fun transferResourceFtsDao(): TransferResourceFtsDao
    abstract fun recentSearchQueryDao(): RecentSearchQueryDao
}
